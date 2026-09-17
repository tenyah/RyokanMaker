package com.mnu.ryokanmaker.domain;
import lombok.Data;

@Data
public class OnsenDTO {
    private Long onsenIdx;
    private String onsenName;
    private String onsenImage;
    private String onsenInfo;
    private Long adminIdx;
    private String onsenSaleYN;
    private String onsenMemo;
    private String onsenHour;
}
