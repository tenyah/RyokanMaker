package com.mnu.ryokanmaker.domain;
import lombok.Data;

@Data
public class PlanDTO {
    private Long planIdx;
    private Long adminIdx;
    private String planName;
    private String planImage;
    private String planIncludesMeal;
    private String planIncludesOnsen;
    private String planInfo;
    private Long planPrice;
    private String planSaleYN;
}