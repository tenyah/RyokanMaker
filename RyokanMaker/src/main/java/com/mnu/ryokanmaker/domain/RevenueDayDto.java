package com.mnu.ryokanmaker.domain;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 매출 캘린더(revenue_monthly.html) 하루치 셀 - 날짜와 해당일 확정 매출 합계. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RevenueDayDto {

    private LocalDate date;
    private Integer amount;
    private boolean future;
}
