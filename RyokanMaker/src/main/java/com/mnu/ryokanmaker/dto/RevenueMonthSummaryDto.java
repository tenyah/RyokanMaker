package com.mnu.ryokanmaker.dto;

import java.time.YearMonth;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 월매출조회(revenue_monthly.html) 한 화면분 데이터. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RevenueMonthSummaryDto {

    private YearMonth yearMonth;
    private int totalRevenue;
    private int occupancyRate;
    private int adr;
    private int startPad;
    private List<RevenueDayDto> days;
    private List<Integer> dowTotals;
}
