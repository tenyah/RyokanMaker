package com.mnu.ryokanmaker.dto;

/**
 * ONSEN 테이블 매핑 DTO (온천 시설 마스터 정보)
 * PK : onsenIdx / FK : adminIdx -> ADMIN.AdminIdx
 */
public class OnsenDto {

    private Integer onsenIdx;
    private String onsenName;
    private String onsenImage;
    private String onsenInfo;
    private Integer adminIdx;
    private String onsenSaleYN;
    private String onsenMemo;

    public OnsenDto() {
    }

    public OnsenDto(Integer onsenIdx, String onsenName, String onsenImage, String onsenInfo,
                     Integer adminIdx, String onsenSaleYN, String onsenMemo) {
        this.onsenIdx = onsenIdx;
        this.onsenName = onsenName;
        this.onsenImage = onsenImage;
        this.onsenInfo = onsenInfo;
        this.adminIdx = adminIdx;
        this.onsenSaleYN = onsenSaleYN;
        this.onsenMemo = onsenMemo;
    }

    public Integer getOnsenIdx() { return onsenIdx; }
    public void setOnsenIdx(Integer onsenIdx) { this.onsenIdx = onsenIdx; }

    public String getOnsenName() { return onsenName; }
    public void setOnsenName(String onsenName) { this.onsenName = onsenName; }

    public String getOnsenImage() { return onsenImage; }
    public void setOnsenImage(String onsenImage) { this.onsenImage = onsenImage; }

    public String getOnsenInfo() { return onsenInfo; }
    public void setOnsenInfo(String onsenInfo) { this.onsenInfo = onsenInfo; }

    public Integer getAdminIdx() { return adminIdx; }
    public void setAdminIdx(Integer adminIdx) { this.adminIdx = adminIdx; }

    public String getOnsenSaleYN() { return onsenSaleYN; }
    public void setOnsenSaleYN(String onsenSaleYN) { this.onsenSaleYN = onsenSaleYN; }

    public String getOnsenMemo() { return onsenMemo; }
    public void setOnsenMemo(String onsenMemo) { this.onsenMemo = onsenMemo; }
}
