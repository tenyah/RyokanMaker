package com.mnu.ryokanmaker.dto;

import java.time.LocalDate;

/**
 * ONSEN_RESERVATION 테이블 매핑 DTO
 * PK : onsenFacilityIdx
 * FK : adminIdx -> ADMIN.AdminIdx, userMail -> MEMBER.UserMail, resvNum -> RESERVATION.Resv_num,
 *      onsenIdx -> ONSEN.OnsenIdx
 */
public class OnsenReservationDto {

    private Integer onsenFacilityIdx;
    private Integer adminIdx;
    private String userMail;
    private Integer resvNum;
    private LocalDate onsenUseDate;
    private String onsenTimeSlot;
    private Integer onsenHeadcount;
    private String onsenStatus;
    private Integer onsenIdx;

    public OnsenReservationDto() {
    }

    public OnsenReservationDto(Integer onsenFacilityIdx, Integer adminIdx, String userMail, Integer resvNum,
                                LocalDate onsenUseDate, String onsenTimeSlot,
                                Integer onsenHeadcount, String onsenStatus, Integer onsenIdx) {
        this.onsenFacilityIdx = onsenFacilityIdx;
        this.adminIdx = adminIdx;
        this.userMail = userMail;
        this.resvNum = resvNum;
        this.onsenUseDate = onsenUseDate;
        this.onsenTimeSlot = onsenTimeSlot;
        this.onsenHeadcount = onsenHeadcount;
        this.onsenStatus = onsenStatus;
        this.onsenIdx = onsenIdx;
    }

    public Integer getOnsenFacilityIdx() { return onsenFacilityIdx; }
    public void setOnsenFacilityIdx(Integer onsenFacilityIdx) { this.onsenFacilityIdx = onsenFacilityIdx; }

    public Integer getAdminIdx() { return adminIdx; }
    public void setAdminIdx(Integer adminIdx) { this.adminIdx = adminIdx; }

    public String getUserMail() { return userMail; }
    public void setUserMail(String userMail) { this.userMail = userMail; }

    public Integer getResvNum() { return resvNum; }
    public void setResvNum(Integer resvNum) { this.resvNum = resvNum; }

    public Integer getOnsenIdx() { return onsenIdx; }
    public void setOnsenIdx(Integer onsenIdx) { this.onsenIdx = onsenIdx; }

    public LocalDate getOnsenUseDate() { return onsenUseDate; }
    public void setOnsenUseDate(LocalDate onsenUseDate) { this.onsenUseDate = onsenUseDate; }

    public String getOnsenTimeSlot() { return onsenTimeSlot; }
    public void setOnsenTimeSlot(String onsenTimeSlot) { this.onsenTimeSlot = onsenTimeSlot; }

    public Integer getOnsenHeadcount() { return onsenHeadcount; }
    public void setOnsenHeadcount(Integer onsenHeadcount) { this.onsenHeadcount = onsenHeadcount; }

    public String getOnsenStatus() { return onsenStatus; }
    public void setOnsenStatus(String onsenStatus) { this.onsenStatus = onsenStatus; }
}
