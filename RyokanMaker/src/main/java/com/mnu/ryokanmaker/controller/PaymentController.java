package com.mnu.ryokanmaker.controller;

import com.mnu.ryokanmaker.domain.AdminDto;
import com.mnu.ryokanmaker.domain.AdminPlanDto;
import com.mnu.ryokanmaker.domain.GuestInfoForm;
import com.mnu.ryokanmaker.domain.MemberDto;
import com.mnu.ryokanmaker.domain.OnsenPickDto;
import com.mnu.ryokanmaker.domain.PriceBreakdownDto;
import com.mnu.ryokanmaker.domain.ReservationContext;
import com.mnu.ryokanmaker.domain.ReservationDto;
import com.mnu.ryokanmaker.domain.ReservationSummary;
import com.mnu.ryokanmaker.domain.RestaurantCourseDto;
import com.mnu.ryokanmaker.domain.RoomDto;
import com.mnu.ryokanmaker.mapper.AdminMapper;
import com.mnu.ryokanmaker.mapper.PlanMapper;
import com.mnu.ryokanmaker.mapper.RestaurantCourseMapper;
import com.mnu.ryokanmaker.mapper.RoomMapper;
import com.mnu.ryokanmaker.service.EmailService;
import com.mnu.ryokanmaker.service.GeminiTranslationService;
import com.mnu.ryokanmaker.service.ExchangeRateService;
import com.mnu.ryokanmaker.service.PaymentReservationService;
import com.mnu.ryokanmaker.service.ReservationService;
import com.mnu.ryokanmaker.service.TossPaymentService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * 예약 정보 입력 · 결제(토스페이먼츠) 관련 화면과 흐름을 전부 담당하는 컨트롤러.
 * 1) /payment          : 결제 화면 진입 (예약 요약 + orderId 발급 + 예약 컨텍스트 세션 저장)
 * 2) /payment/prepare  : 예약자 정보 확인 후 결제대기 상태로 예약을 DB에 저장
 * 3) /payment/success  : 결제 인증 성공 콜백 → 저장된 금액과 대조 후 승인(confirm) → 결제완료로 갱신
 * 4) /payment/fail     : 결제 인증 실패/취소 콜백
 *
 * RESERVATION/ROOM_RESERVATION의 USER_MAIL이 MEMBER를 FK로 참조하므로 로그인 회원만 예약할 수 있다.
 */
@Controller
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    private static final String SESSION_RESERVATION_CONTEXT = "RESERVATION_CONTEXT_";

    private final TossPaymentService tossPaymentService;

    @Autowired
    private PlanMapper planMapper;

    @Autowired
    private RoomMapper roomMapper;

    @Autowired
    private RestaurantCourseMapper restaurantCourseMapper;

    @Autowired
    private PaymentReservationService paymentReservationService;

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ExchangeRateService exchangeRateService;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private GeminiTranslationService translationService;

    @Value("${tosspayments.client-key}")
    private String tossClientKey;

    public PaymentController(TossPaymentService tossPaymentService) {
        this.tossPaymentService = tossPaymentService;
    }

    /**
     * 예약 정보 입력 · 결제 화면 진입 : templates/payment/payment.html
     * reservation/reservation.html(숙박예약 화면)에서 객실·플랜·코스·온천을 선택하고
     * 결제 페이지로 이동을 누르면 그 선택값이 쿼리 파라미터로 여기에 전달된다.
     */
    @GetMapping("/payment")
    public String payment(@RequestParam String planCode,
                           @RequestParam Long roomIdx,
                           @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
                           @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
                           @RequestParam Long totalAmount,
                           @RequestParam(defaultValue = "2") int adultCount,
                           @RequestParam(defaultValue = "0") int childCount,
                           @RequestParam(defaultValue = "1") int roomCount,
                           @RequestParam(required = false) Long courseIdx,
                           @RequestParam(required = false) List<String> onsen,
                           Model model, HttpSession session) {

        MemberDto member = loginMember(session);
        if (member == null) {
            return "redirect:/member/login";
        }

        AdminPlanDto plan = planMapper.findById(Integer.valueOf(planCode));
        RoomDto room = roomMapper.findById(roomIdx.intValue());
        RestaurantCourseDto course = courseIdx != null
                ? restaurantCourseMapper.findAllOnSale().stream()
                        .filter(c -> c.getRestaurantCourseIdx().equals(courseIdx.intValue()))
                        .findFirst().orElse(null)
                : null;

        // 화면에서 넘어온 totalAmount는 위변조될 수 있으므로 서버에서 다시 계산한 금액을 사용한다.
        // onsen : 날짜별 온천 선택. 각 값은 "날짜|온천idx|시간" 형식 (예: 2026-09-19|1|15:00)
        List<OnsenPickDto> onsenPicks = reservationService.parseOnsenPicks(onsen, checkIn, checkOut);
        int nights = (int) ChronoUnit.DAYS.between(checkIn, checkOut);
        PriceBreakdownDto priceJpy = reservationService.calculatePriceBreakdown(
                Integer.valueOf(planCode), roomIdx.intValue(),
                courseIdx != null ? courseIdx.intValue() : null,
                onsenPicks, nights);

        // DB 가격은 전부 엔화(JPY) 기준이지만, 실제 결제(TossPayments)는 원화로만 되기 때문에
        // 결제 화면/실제 청구 금액은 여기서 원화로 한 번에 환산해서 확정한다.
        PriceBreakdownDto price = new PriceBreakdownDto(
                (int) exchangeRateService.toKrw(priceJpy.getPlanFee()),
                (int) exchangeRateService.toKrw(priceJpy.getRoomExtra()),
                priceJpy.getCourseExtra() != null ? (int) exchangeRateService.toKrw(priceJpy.getCourseExtra()) : null,
                priceJpy.getOnsenExtra() != null ? (int) exchangeRateService.toKrw(priceJpy.getOnsenExtra()) : null);
        int finalAmount = price.getTotal();


        Locale locale = LocaleContextHolder.getLocale();
        StringBuilder planDescription = new StringBuilder();
        if (course != null) {
            String courseName = translationService.translate(course.getRestaurantCourseName(), locale);
            planDescription.append(messageSource.getMessage("pay.includes_course", new Object[]{courseName}, locale));
        }
        for (OnsenPickDto pick : onsenPicks) {
            if (planDescription.length() > 0) planDescription.append(" · ");
            String when = pick.getDate().getMonthValue() + "/" + pick.getDate().getDayOfMonth() + " " + pick.getTimeSlot();
            planDescription.append(messageSource.getMessage("pay.onsen_use", new Object[]{when}, locale));
        }

        GuestInfoForm guestInfoForm = new GuestInfoForm();
        // 토스페이먼츠 orderId 규칙: 영문/숫자/-_, 6~64자, 결제마다 고유해야 함
        guestInfoForm.setOrderId("RYOKAN-" + UUID.randomUUID().toString().replace("-", ""));
        // 기본은 로그인한 회원 본인 정보. 다른 사람 이름으로 예약할 때는 화면에서 지우고 직접 입력한다.
        guestInfoForm.setLastNameJp(member.getUserLastNameJp());
        guestInfoForm.setFirstNameJp(member.getUserFirstNameJp());
        guestInfoForm.setLastNameEn(member.getUserLastNameEn());
        guestInfoForm.setFirstNameEn(member.getUserFirstNameEn());
        guestInfoForm.setEmail(member.getUserMail());
        guestInfoForm.setPhone(member.getUserTel());
        guestInfoForm.setCountry(countryCodeOf(member.getUserCountry()));

        // 결제창을 띄우기 직전(/payment/prepare)에 이 선택값으로 예약을 저장한다.
        session.setAttribute(SESSION_RESERVATION_CONTEXT + guestInfoForm.getOrderId(),
                new ReservationContext(
                        room != null ? room.getAdminIdx() : null,
                        Integer.valueOf(planCode),
                        roomIdx.intValue(),
                        checkIn, checkOut,
                        finalAmount,
                        adultCount + childCount,
                        courseIdx != null ? courseIdx.intValue() : null,
                        onsenPicks));

        model.addAttribute("guestInfoForm", guestInfoForm);
        model.addAttribute("reservation", new ReservationSummary(
                plan != null ? plan.getPlanName() : "",
                checkIn, checkOut,
                nights, roomCount, adultCount, childCount,
                room != null ? room.getRoomName() : "",
                planDescription.toString(),
                price.getPlanFee(), price.getRoomExtra(), price.getCourseExtra(), price.getOnsenExtra(), finalAmount,
                3
        ));
        model.addAttribute("countryOptions", buildCountryOptions());
        model.addAttribute("arrivalTimeOptions", buildArrivalTimeOptions());
        model.addAttribute("tossClientKey", tossClientKey);

        return "payment/payment";
    }

    /**
     * 결제창을 띄우기 직전에 호출된다. 예약자 정보를 세션에 보관하고
     * 예약을 결제대기 상태로 DB에 저장한다 (승인되면 success에서 결제완료로 갱신).
     */
    @PostMapping("/payment/prepare")
    @ResponseBody
    public ResponseEntity<String> prepare(@ModelAttribute GuestInfoForm guestInfoForm, HttpSession session) {
        MemberDto member = loginMember(session);
        if (member == null) {
            return ResponseEntity.status(401).body("login required");
        }

        ReservationContext context =
                (ReservationContext) session.getAttribute(SESSION_RESERVATION_CONTEXT + guestInfoForm.getOrderId());
        if (context == null) {
            return ResponseEntity.badRequest().body("reservation expired");
        }
        // RESERVATION 예약자 컬럼 제약(NOT NULL, VARCHAR2 바이트 길이)을 저장 전에 확인한다
        if (isBlank(guestInfoForm.getLastNameEn()) || isBlank(guestInfoForm.getFirstNameEn())
                || isBlank(guestInfoForm.getEmail()) || isBlank(guestInfoForm.getCountry())
                || isBlank(guestInfoForm.getPhone())
                || tooLong(guestInfoForm.getLastNameEn(), 50) || tooLong(guestInfoForm.getFirstNameEn(), 50)
                || tooLong(guestInfoForm.getLastNameJp(), 50) || tooLong(guestInfoForm.getFirstNameJp(), 50)
                || tooLong(guestInfoForm.getEmail(), 100) || tooLong(guestInfoForm.getCountry(), 50)
                || tooLong(guestInfoForm.getPhone(), 20)) {
            return ResponseEntity.badRequest().body("invalid guest info");
        }
        if (!isKanaOrBlank(guestInfoForm.getLastNameJp()) || !isKanaOrBlank(guestInfoForm.getFirstNameJp())) {
            return ResponseEntity.badRequest().body("invalid jp name");
        }

        session.setAttribute("guestInfoForm", guestInfoForm);

        try {
            paymentReservationService.saveAsWaiting(context, guestInfoForm, member.getUserMail());
        } catch (RuntimeException e) {
            log.error("예약 저장 실패 (orderId={})", guestInfoForm.getOrderId(), e);
            return ResponseEntity.internalServerError().body("reservation save failed");
        }

        return ResponseEntity.ok("ok");
    }

    @GetMapping("/payment/success")
    public String success(@RequestParam String paymentKey,
                           @RequestParam String orderId,
                           @RequestParam int amount,
                           HttpSession session,
                           Model model) {

        // 서버가 신뢰하는 금액은 DB에 저장된 예약 금액이다 (successUrl의 amount는 위변조 가능).
        Integer expectedAmount = paymentReservationService.findTrustedAmount(orderId);
        if (expectedAmount == null || expectedAmount != amount) {
            model.addAttribute("messageKey", "pay.error.amount_mismatch");
            return "payment/fail";
        }

        Map<String, Object> result = tossPaymentService.confirm(paymentKey, orderId, amount);

        Object method = result.get("method");
        paymentReservationService.markAsPaid(orderId, method != null ? method.toString() : null);

        sendReservationMail(session, orderId);

        session.removeAttribute(SESSION_RESERVATION_CONTEXT + orderId);

        model.addAttribute("orderId", orderId);
        model.addAttribute("amount", amount);
        model.addAttribute("result", result);
        return "payment/success";
    }

    @GetMapping("/payment/fail")
    public String fail(@RequestParam(required = false) String code,
                        @RequestParam(required = false) String message,
                        Model model) {
        model.addAttribute("code", code);
        model.addAttribute("message", message);
        return "payment/fail";
    }

    /** 결제 승인 후 예약 완료 메일 발송. 세션이 만료돼 예약 정보를 못 찾으면 건너뛴다(예약 자체는 이미 저장됨). */
    private void sendReservationMail(HttpSession session, String orderId) {
        MemberDto member = loginMember(session);
        ReservationContext context = (ReservationContext) session.getAttribute(SESSION_RESERVATION_CONTEXT + orderId);
        if (context == null) {
            log.warn("예약 완료 메일 생략: 세션에 예약 정보가 없음 (orderId={})", orderId);
            return;
        }
        try {
            // 수신자는 로그인 회원이 아니라 결제 화면에서 입력한 예약자 메일(RESV_MAIL)
            ReservationDto guest = paymentReservationService.findGuestByOrderId(orderId);
            if (guest == null || guest.getResvMail() == null) {
                log.warn("예약 완료 메일 생략: 예약자 메일이 없음 (orderId={})", orderId);
                return;
            }
            // 본인 예약이면 닉네임, 다른 분 예약이면 입력한 영문 이름으로 부른다
            String guestName = (member != null && guest.getResvMail().equalsIgnoreCase(member.getUserMail()))
                    ? member.getUserNickname()
                    : guest.getResvLastNameEn() + " " + guest.getResvFirstNameEn();

            RoomDto room = roomMapper.findById(context.getRoomIdx());
            AdminPlanDto plan = planMapper.findById(context.getPlanIdx());
            AdminDto admin = adminMapper.selectByAdminIdx(context.getAdminIdx());
            emailService.sendReservationConfirmed(guest.getResvMail(), guestName,
                    admin != null ? admin.getRyokanName() : null, orderId,
                    room != null ? room.getRoomName() : null, plan != null ? plan.getPlanName() : null,
                    context.getCheckIn(), context.getCheckOut(), context.getPeople(), context.getTotalAmount());
        } catch (RuntimeException e) {
            log.warn("예약 완료 메일 준비 실패 (orderId={})", orderId, e);
        }
    }

    private MemberDto loginMember(HttpSession session) {
        return (MemberDto) session.getAttribute("loginMember");
    }

    // ---------------------------------------------------------------
    // 아래는 전부 화면 확인용 더미 데이터입니다. (실제로는 Service에서 조회)
    // ---------------------------------------------------------------

    // 일본어 이름: 히라가나(3040-309F)·가타카나(30A0-30FF, 장음 ー 포함)·반각 가타카나(FF65-FF9F)와 공백만 허용.
    // payment.html의 입력창 pattern과 같은 범위여야 한다.
    private static final java.util.regex.Pattern KANA_NAME =
            java.util.regex.Pattern.compile("[\\u3040-\\u309F\\u30A0-\\u30FF\\uFF65-\\uFF9F\\u3000 ]+");

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    // Oracle VARCHAR2 길이는 바이트 기준(UTF-8에서 가나 1글자 = 3바이트). 저장 시 앞뒤 공백은 제거된다.
    private static boolean tooLong(String s, int maxBytes) {
        return s != null && s.strip().getBytes(StandardCharsets.UTF_8).length > maxBytes;
    }

    /** 일본어 이름은 선택 입력이라 비어 있으면 통과, 입력했다면 가나만 허용. */
    private static boolean isKanaOrBlank(String name) {
        return name == null || name.isBlank() || KANA_NAME.matcher(name).matches();
    }

    /** 회원 국가(COUNTRY_CODE 테이블의 국가명)를 결제 화면 선택지 코드로 바꾼다. */
    private static String countryCodeOf(String countryName) {
        if (countryName == null || countryName.isBlank()) {
            return null;
        }
        return switch (countryName.strip()) {
            case "대한민국" -> "KR";
            case "일본" -> "JP";
            case "미국" -> "US";
            case "중국" -> "CN";
            case "대만" -> "TW";
            default -> "ETC";
        };
    }

    private Map<String, String> buildCountryOptions() {
        Map<String, String> countries = new LinkedHashMap<>();
        countries.put("KR", "country.KR");
        countries.put("JP", "country.JP");
        countries.put("US", "country.US");
        countries.put("CN", "country.CN");
        countries.put("TW", "country.TW");
        countries.put("ETC", "country.ETC");
        return countries;
    }

    private Map<String, String> buildArrivalTimeOptions() {
        Map<String, String> times = new LinkedHashMap<>();
        times.put("15", "arrival.15");
        times.put("16", "arrival.16");
        times.put("17", "arrival.17");
        times.put("18", "arrival.18");
        times.put("19", "arrival.19");
        return times;
    }
}
