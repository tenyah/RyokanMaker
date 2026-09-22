package com.mnu.ryokanmaker.domain;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 판매 관리(plan_sales.html) 표 한 행 : 플랜 1개 + 조회 기간 날짜별 예약 건수. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlanSalesRowDto {

    private AdminPlanDto plan;
    private List<PlanDayStatusDto> days;
}
