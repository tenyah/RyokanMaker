package com.mnu.ryokanmaker.controller;

import com.mnu.ryokanmaker.dto.AdminPlanDto;
import com.mnu.ryokanmaker.dto.GuestInfoForm;
import com.mnu.ryokanmaker.dto.ReservationSummary;
import com.mnu.ryokanmaker.dto.RestaurantCourseDto;
import com.mnu.ryokanmaker.dto.RoomDto;
import com.mnu.ryokanmaker.mapper.PlanMapper;
import com.mnu.ryokanmaker.mapper.RestaurantCourseMapper;
import com.mnu.ryokanmaker.mapper.RoomMapper;
import com.mnu.ryokanmaker.service.TossPaymentService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 예약 정보 입력 · 결제(토스페이먼츠) 관련 화면과 흐름을 전부 담당하는 컨트롤러.
 * 1) /payment          : 결제 화면 진입 (예약 요약 + orderId 발급)
 * 2) /payment/prepare  : 예약자 정보 저장 + 서버가 신뢰하는 주문 금액을 세션에 기록
 * 3) /payment/success  : 결제 인증 성공 콜백 → 서버 저장 금액과 대조 후 승인(confirm) API 호출
 * 4) /payment/fail     : 결제 인증 실패/취소 콜백
 */
@Controller
public class PaymentController {

    private static final String SESSION_ORDER_AMOUNT_PREFIX = "TOSS_ORDER_AMOUNT_";

    private final TossPaymentService tossPaymentService;

    @Autowired
    private PlanMapper planMapper;

    @Autowired
    private RoomMapper roomMapper;

    @Autowired
    private RestaurantCourseMapper restaurantCourseMapper;

    @Value("${tosspayments.client-key}")
    private String tossClientKey;

    public PaymentController(TossPaymentService tossPaymentService) {
        this.tossPaymentService = tossPaymentService;
    }

    /**
     * 예약 정보 입력 · 결제 화면 진입 : templates/payment/payment.html
     * reservation/reservation.html(숙박예약 화면)에서 객실·플랜·코스·온천을 선택하고
     * "결제 페이지로 이동"을 누르면 그 선택값이 쿼리 파라미터로 여기에 전달된다.
     */
    @GetMapping("/payment")
    public String payment(@RequestParam String planCode,
                           @RequestParam Long roomIdx,
                           @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
                           @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
                           @RequestParam Long totalAmount,
                           @RequestParam(required = false) Long courseIdx,
                           @RequestParam(required = false) Long onsenIdx,
                           @RequestParam(required = false) String onsenTimeSlot,
                           Model model, HttpSession session) {

        AdminPlanDto plan = planMapper.findById(Integer.valueOf(planCode));
        RoomDto room = roomMapper.findById(roomIdx.intValue());
        RestaurantCourseDto course = courseIdx != null
                ? restaurantCourseMapper.findAllOnSale().stream()
                        .filter(c -> c.getRestaurantCourseIdx().equals(courseIdx.intValue()))
                        .findFirst().orElse(null)
                : null;

        int nights = (int) ChronoUnit.DAYS.between(checkIn, checkOut);
        int roomFee = room != null ? room.getRoomPrice() : 0;
        int mealFee = totalAmount.intValue() - roomFee;

        StringBuilder planDescription = new StringBuilder();
        if (course != null) {
            planDescription.append(course.getRestaurantCourseName()).append(" 포함");
        }
        if (onsenTimeSlot != null) {
            if (planDescription.length() > 0) planDescription.append(" · ");
            planDescription.append("온천 ").append(onsenTimeSlot).append(" 이용");
        }

        GuestInfoForm guestInfoForm = new GuestInfoForm();
        // 토스페이먼츠 orderId 규칙: 영문/숫자/-_, 6~64자, 결제마다 고유해야 함
        guestInfoForm.setOrderId("RYOKAN-" + UUID.randomUUID().toString().replace("-", ""));

        // 예약 화면에서 계산된 총 결제 금액을 서버 신뢰 값으로 세션에 저장 (prepare가 사용)
        session.setAttribute(SESSION_ORDER_AMOUNT_PREFIX + guestInfoForm.getOrderId(), totalAmount.intValue());

        model.addAttribute("guestInfoForm", guestInfoForm);
        model.addAttribute("reservation", new ReservationSummary(
                plan != null ? plan.getPlanName() : "",
                checkIn, checkOut,
                nights, 1, 2, 0,
                room != null ? room.getRoomName() : "",
                planDescription.toString(),
                roomFee, mealFee, totalAmount.intValue(),
                3
        ));
        model.addAttribute("countryOptions", buildCountryOptions());
        model.addAttribute("arrivalTimeOptions", buildArrivalTimeOptions());
        model.addAttribute("tossClientKey", tossClientKey);

        // TODO: 결제 담당자 - 여기서 회원 정보 조회, 결제 수단 처리, RESERVATION/ROOM_RESERVATION 등 INSERT 로직 추가

        return "payment/payment";
    }

    @PostMapping("/payment/prepare")
    @ResponseBody
    public String prepare(@ModelAttribute GuestInfoForm guestInfoForm, HttpSession session) {
        // TODO: 실제로는 여기서 GuestInfoForm 유효성(이메일 일치, 약관 동의 등)을 검증하고
        //       예약 정보를 DB에 저장해야 함. 지금은 화면 확인용이라 세션에만 보관.
        session.setAttribute("guestInfoForm", guestInfoForm);

        // 결제 요청 시점의 총 결제 금액은 /payment 진입 시 이미 세션에 저장해뒀다(위조 방지).
        // successUrl에서 돌아온 amount는 클라이언트가 위변조할 수 있으므로,
        // confirm API 호출 전 반드시 이 값과 대조해야 한다.

        return "ok";
    }

    @GetMapping("/payment/success")
    public String success(@RequestParam String paymentKey,
                           @RequestParam String orderId,
                           @RequestParam int amount,
                           HttpSession session,
                           Model model) {

        Integer expectedAmount = (Integer) session.getAttribute(SESSION_ORDER_AMOUNT_PREFIX + orderId);
        if (expectedAmount == null || expectedAmount != amount) {
            model.addAttribute("message", "결제 금액이 일치하지 않습니다. 다시 시도해주세요.");
            return "payment/fail";
        }

        Map<String, Object> result = tossPaymentService.confirm(paymentKey, orderId, amount);

        session.removeAttribute(SESSION_ORDER_AMOUNT_PREFIX + orderId);

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

    // ---------------------------------------------------------------
    // 아래는 전부 화면 확인용 더미 데이터입니다. (실제로는 Service에서 조회)
    // ---------------------------------------------------------------

    private Map<String, String> buildCountryOptions() {
        Map<String, String> countries = new LinkedHashMap<>();
        countries.put("KR", "대한민국");
        countries.put("JP", "일본");
        countries.put("US", "미국");
        countries.put("CN", "중국");
        countries.put("TW", "대만");
        countries.put("ETC", "그 외");
        return countries;
    }

    private Map<String, String> buildArrivalTimeOptions() {
        Map<String, String> times = new LinkedHashMap<>();
        times.put("15", "15:00 ~ 16:00");
        times.put("16", "16:00 ~ 17:00");
        times.put("17", "17:00 ~ 18:00");
        times.put("18", "18:00 ~ 19:00");
        times.put("19", "19:00 이후");
        return times;
    }
}
