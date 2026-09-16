package com.mnu.ryokanmaker.dto;

import java.time.LocalDate;

/** 관리자 - 예약 현황 상세 패널의 온천 예약 1행 (ONSEN_RESERVATION + ONSEN 조인). */
public class AdminReservationOnsenItemDto {

    private String onsenName;
    private LocalDate onsenUseDate;
    private String onsenTimeSlot;
    private Integer onsenHeadcount;
    private String onsenStatus;

    public AdminReservationOnsenItemDto() {
    }

    public String getOnsenName() { return onsenName; }
    public void setOnsenName(String onsenName) { this.onsenName = onsenName; }

    public LocalDate getOnsenUseDate() { return onsenUseDate; }
    public void setOnsenUseDate(LocalDate onsenUseDate) { this.onsenUseDate = onsenUseDate; }

    public String getOnsenTimeSlot() { return onsenTimeSlot; }
    public void setOnsenTimeSlot(String onsenTimeSlot) { this.onsenTimeSlot = onsenTimeSlot; }

    public Integer getOnsenHeadcount() { return onsenHeadcount; }
    public void setOnsenHeadcount(Integer onsenHeadcount) { this.onsenHeadcount = onsenHeadcount; }

    public String getOnsenStatus() { return onsenStatus; }
    public void setOnsenStatus(String onsenStatus) { this.onsenStatus = onsenStatus; }
}
