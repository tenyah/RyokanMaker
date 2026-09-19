package com.mnu.ryokanmaker.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 판매 관리(plan_sales.html) 표 한 칸 : 특정 플랜의 특정 날짜(체크인 기준) 예약 건수. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlanDayStatusDto {

    private LocalDate day;
    private long bookedCount;
}
