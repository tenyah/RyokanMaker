package com.mnu.ryokanmaker.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 결제 화면(payment.html) 우측 사이드바에 표시되는 예약 요약 정보 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationSummary {

    private String planBadge;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private int nights;
    private int roomCount;
    private int adultCount;
    private int childCount;
    private String roomPlanName;
    private String roomPlanDescription;
    private int planFee;           // 플랜 기본 요금
    private int roomExtra;         // 객실 추가 요금
    private Integer courseExtra;   // 식사 코스 추가 요금 (코스를 고르지 않았으면 null)
    private Integer onsenExtra;    // 온천 추가 요금 합계 (온천을 고르지 않았으면 null)
    private int totalAmount;
    private int cancelPolicyDays;
}
