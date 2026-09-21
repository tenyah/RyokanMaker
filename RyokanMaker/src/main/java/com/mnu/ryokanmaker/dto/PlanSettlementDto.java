package com.mnu.ryokanmaker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 플랜별 매출 정산 (가로 막대) 한 줄. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlanSettlementDto {

    private String planName;
    private Integer amount;
}
