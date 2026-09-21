package com.mnu.ryokanmaker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 결제금액 구성. courseExtra / onsenExtra는 해당 항목을 고르지 않았으면 null. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceBreakdownDto {

    private int planFee;
    private int roomExtra;
    private Integer courseExtra;
    private Integer onsenExtra;

    public int getTotal() {
        return planFee + roomExtra
                + (courseExtra != null ? courseExtra : 0)
                + (onsenExtra != null ? onsenExtra : 0);
    }
}
