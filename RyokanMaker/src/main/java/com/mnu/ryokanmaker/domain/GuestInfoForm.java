package com.mnu.ryokanmaker.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

/** 결제 화면(payment.html)의 예약자 정보 + 결제 수단 입력 폼 바인딩 객체 */
@Data
@NoArgsConstructor
public class GuestInfoForm {

    // 예약자 정보
    private String lastNameJp;
    private String firstNameJp;
    private String lastNameEn;
    private String firstNameEn;
    private String email;
    private String country;
    private String phone;
    private String arrivalTime;
    private String requestNote;

    // 토스페이먼츠 주문서형 결제 식별자 (서버에서 생성해 hidden input으로 전달)
    private String orderId;

    // 약관 동의
    private boolean agreeCancelPolicy;
    private boolean agreePrivacyPolicy;
}
