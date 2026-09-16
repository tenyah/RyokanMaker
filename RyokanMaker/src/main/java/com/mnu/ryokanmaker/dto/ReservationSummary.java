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
    private int roomFee;
    private int mealFee;
    private int totalAmount;
    private int cancelPolicyDays;
}
