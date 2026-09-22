package com.mnu.ryokanmaker.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 일자별 매출조회의 결제수단별 집계 한 줄 (실제로 쓰인 수단만 나타난다). */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMethodStatDto {

    private String method;
    private int count;
    private int total;
    private int percent;
}
