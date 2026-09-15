package com.mnu.RyokanMaker.dto;

import java.time.LocalDate;

/**
 * ONSEN_RESERVATION 테이블 매핑 DTO
 * PK : onsenFacilityIdx
 * FK : adminIdx -> ADMIN.AdminIdx, userMail -> MEMBER.UserMail, resvNum -> RESERVATION.Resv_num
 */
public class OnsenReservationDto {

    private Integer onsenFacilityIdx;
    private Integer adminIdx;
    private String userMail;
    private Integer resvNum;
    private String onsenType;
    private LocalDate onsenUseDate;
    private String onsenTimeSlot;
    private Integer onsenHeadcount;
    private String onsenStatus;

    public OnsenReservationDto() {
    }

    public OnsenReservationDto(Integer onsenFacilityIdx, Integer adminIdx, String userMail, Integer resvNum,
                                String onsenType, LocalDate onsenUseDate, String onsenTimeSlot,
                                Integer onsenHeadcount, String onsenStatus) {
        this.onsenFacilityIdx = onsenFacilityIdx;
        this.adminIdx = adminIdx;
        this.userMail = userMail;
        this.resvNum = resvNum;
        this.onsenType = onsenType;
        this.onsenUseDate = onsenUseDate;
        this.onsenTimeSlot = onsenTimeSlot;
        this.onsenHeadcount = onsenHeadcount;
        this.onsenStatus = onsenStatus;
    }

    public Integer getOnsenFacilityIdx() { return onsenFacilityIdx; }
    public void setOnsenFacilityIdx(Integer onsenFacilityIdx) { this.onsenFacilityIdx = onsenFacilityIdx; }

    public Integer getAdminIdx() { return adminIdx; }
    public void setAdminIdx(Integer adminIdx) { this.adminIdx = adminIdx; }

    public String getUserMail() { return userMail; }
    public void setUserMail(String userMail) { this.userMail = userMail; }

    public Integer getResvNum() { return resvNum; }
    public void setResvNum(Integer resvNum) { this.resvNum = resvNum; }

    public String getOnsenType() { return onsenType; }
    public void setOnsenType(String onsenType) { this.onsenType = onsenType; }

    public LocalDate getOnsenUseDate() { return onsenUseDate; }
    public void setOnsenUseDate(LocalDate onsenUseDate) { this.onsenUseDate = onsenUseDate; }

    public String getOnsenTimeSlot() { return onsenTimeSlot; }
    public void setOnsenTimeSlot(String onsenTimeSlot) { this.onsenTimeSlot = onsenTimeSlot; }

    public Integer getOnsenHeadcount() { return onsenHeadcount; }
    public void setOnsenHeadcount(Integer onsenHeadcount) { this.onsenHeadcount = onsenHeadcount; }

    public String getOnsenStatus() { return onsenStatus; }
    public void setOnsenStatus(String onsenStatus) { this.onsenStatus = onsenStatus; }
}
