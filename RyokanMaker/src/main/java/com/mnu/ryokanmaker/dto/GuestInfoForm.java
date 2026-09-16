package com.mnu.ryokanmaker.dto;

/** 결제 화면(payment.html)의 예약자 정보 + 결제 수단 입력 폼 바인딩 객체 */
public class GuestInfoForm {

    // 예약자 정보
    private String lastNameJp;
    private String firstNameJp;
    private String lastNameEn;
    private String firstNameEn;
    private String email;
    private String emailConfirm;
    private String country;
    private String phone;
    private String arrivalTime;
    private String requestNote;

    // 토스페이먼츠 주문서형 결제 식별자 (서버에서 생성해 hidden input으로 전달)
    private String orderId;

    // 약관 동의
    private boolean agreeCancelPolicy;
    private boolean agreePrivacyPolicy;

    public GuestInfoForm() {
    }

    public String getLastNameJp() { return lastNameJp; }
    public void setLastNameJp(String lastNameJp) { this.lastNameJp = lastNameJp; }

    public String getFirstNameJp() { return firstNameJp; }
    public void setFirstNameJp(String firstNameJp) { this.firstNameJp = firstNameJp; }

    public String getLastNameEn() { return lastNameEn; }
    public void setLastNameEn(String lastNameEn) { this.lastNameEn = lastNameEn; }

    public String getFirstNameEn() { return firstNameEn; }
    public void setFirstNameEn(String firstNameEn) { this.firstNameEn = firstNameEn; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getEmailConfirm() { return emailConfirm; }
    public void setEmailConfirm(String emailConfirm) { this.emailConfirm = emailConfirm; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getArrivalTime() { return arrivalTime; }
    public void setArrivalTime(String arrivalTime) { this.arrivalTime = arrivalTime; }

    public String getRequestNote() { return requestNote; }
    public void setRequestNote(String requestNote) { this.requestNote = requestNote; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public boolean isAgreeCancelPolicy() { return agreeCancelPolicy; }
    public void setAgreeCancelPolicy(boolean agreeCancelPolicy) { this.agreeCancelPolicy = agreeCancelPolicy; }

    public boolean isAgreePrivacyPolicy() { return agreePrivacyPolicy; }
    public void setAgreePrivacyPolicy(boolean agreePrivacyPolicy) { this.agreePrivacyPolicy = agreePrivacyPolicy; }
}
