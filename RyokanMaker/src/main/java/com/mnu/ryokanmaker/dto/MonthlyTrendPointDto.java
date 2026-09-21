package com.mnu.ryokanmaker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 월별 매출 추이 차트의 한 달치 포인트 (올해 vs 전년, 체크인 날짜 기준). */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyTrendPointDto {

    private int month;
    private Integer currentYearAmount;
    private Integer prevYearAmount;
}
