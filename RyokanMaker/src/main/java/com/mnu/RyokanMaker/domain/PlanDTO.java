package com.mnu.RyokanMaker.domain;

import lombok.Data;

@Data
public class PlanDTO {
    private int planIdx;
    private int adminIdx;
    private String planName;
    private String planImage;
    private String planIncludesMeal;   // "Y" / "N"
    private String planIncludesOnsen;  // "Y" / "N"
    private String planInfo;
    private int planPrice;
    private String plansaleyn;
    private String planMemo;

}
