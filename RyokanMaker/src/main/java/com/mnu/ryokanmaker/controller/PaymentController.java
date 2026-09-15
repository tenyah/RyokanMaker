package com.mnu.ryokanmaker.controller;

import com.mnu.ryokanmaker.dto.GuestInfoForm;
import com.mnu.ryokanmaker.dto.ReservationSummary;
import com.mnu.ryokanmaker.service.TossPaymentService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;
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

    @Value("${tosspayments.client-key}")
    private String tossClientKey;

    public PaymentController(TossPaymentService tossPaymentService) {
        this.tossPaymentService = tossPaymentService;
    }

    /** 예약 정보 입력 · 결제 화면 : templates/payment/payment.html */
    @GetMapping("/payment")
    public String payment(Model model) {

        LocalDate checkIn = LocalDate.of(2026, 9, 13);
        LocalDate checkOut = checkIn.plusDays(1);

        GuestInfoForm guestInfoForm = new GuestInfoForm();
        // 토스페이먼츠 orderId 규칙: 영문/숫자/-_, 6~64자, 결제마다 고유해야 함
        guestInfoForm.setOrderId("RYOKAN-" + UUID.randomUUID().toString().replace("-", ""));

        model.addAttribute("guestInfoForm", guestInfoForm);
        model.addAttribute("reservation", new ReservationSummary(
                "객실 + 식사 플랜",
                checkIn, checkOut,
                1, 1, 2, 0,
                "사쿠라", "달 코스『계절의 맛 가이세키』포함",
                780000, 68508, 848508,
                3
        ));
        model.addAttribute("countryOptions", buildCountryOptions());
        model.addAttribute("arrivalTimeOptions", buildArrivalTimeOptions());
        model.addAttribute("tossClientKey", tossClientKey);

        return "payment/payment";
    }

    @PostMapping("/payment/prepare")
    @ResponseBody
    public String prepare(@ModelAttribute GuestInfoForm guestInfoForm, HttpSession session) {
        // TODO: 실제로는 여기서 GuestInfoForm 유효성(이메일 일치, 약관 동의 등)을 검증하고
        //       예약 정보를 DB에 저장해야 함. 지금은 화면 확인용이라 세션에만 보관.
        session.setAttribute("guestInfoForm", guestInfoForm);

        // 결제 요청 시점의 총 결제 금액을 서버 신뢰 값으로 세션에 저장해둔다.
        // successUrl에서 돌아온 amount는 클라이언트가 위변조할 수 있으므로,
        // confirm API 호출 전 반드시 이 값과 대조해야 한다.
        session.setAttribute(SESSION_ORDER_AMOUNT_PREFIX + guestInfoForm.getOrderId(), 848508);

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
