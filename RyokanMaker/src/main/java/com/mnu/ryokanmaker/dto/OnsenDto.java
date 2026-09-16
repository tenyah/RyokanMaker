package com.mnu.ryokanmaker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ONSEN 테이블 매핑 DTO (온천 시설 마스터 정보)
 * PK : onsenIdx / FK : adminIdx -> ADMIN.ADMIN_IDX
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OnsenDto {

    private Integer onsenIdx;
    private String onsenName;
    private String onsenImage;
    private String onsenInfo;
    private Integer adminIdx;
    private String onsenSaleYN;
    private String onsenMemo;
}
