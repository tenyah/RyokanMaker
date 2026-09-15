package com.mnu.RyokanMaker.dto;

import java.time.LocalDate;

/**
 * ROOM_RESERVATION 테이블 매핑 DTO (객실 예약 상세 - 체크인/아웃, 플랜 등)
 * PK : roomResvNum
 * FK : userMail -> MEMBER.UserMail, adminIdx -> ADMIN.AdminIdx,
 *      roomIdx -> ROOM.RoomIdx, resvNum -> RESERVATION.Resv_num
 */
public class RoomReservationDto {

    private Integer roomResvNum;
    private String userMail;
    private Integer adminIdx;
    private Integer roomIdx;
    private Integer resvNum;
    private LocalDate resvCheckIn;
    private LocalDate resvCheckOut;
    private Integer resvPrice;
    private Integer resvPeople;
    private String resvStatus;
    private String resvRoomPlan;
    private String resvPayStatus;
    private String resvPayMethod;

    public RoomReservationDto() {
    }

    public RoomReservationDto(Integer roomResvNum, String userMail, Integer adminIdx, Integer roomIdx,
                               Integer resvNum, LocalDate resvCheckIn, LocalDate resvCheckOut,
                               Integer resvPrice, Integer resvPeople, String resvStatus,
                               String resvRoomPlan, String resvPayStatus, String resvPayMethod) {
        this.roomResvNum = roomResvNum;
        this.userMail = userMail;
        this.adminIdx = adminIdx;
        this.roomIdx = roomIdx;
        this.resvNum = resvNum;
        this.resvCheckIn = resvCheckIn;
        this.resvCheckOut = resvCheckOut;
        this.resvPrice = resvPrice;
        this.resvPeople = resvPeople;
        this.resvStatus = resvStatus;
        this.resvRoomPlan = resvRoomPlan;
        this.resvPayStatus = resvPayStatus;
        this.resvPayMethod = resvPayMethod;
    }

    public Integer getRoomResvNum() { return roomResvNum; }
    public void setRoomResvNum(Integer roomResvNum) { this.roomResvNum = roomResvNum; }

    public String getUserMail() { return userMail; }
    public void setUserMail(String userMail) { this.userMail = userMail; }

    public Integer getAdminIdx() { return adminIdx; }
    public void setAdminIdx(Integer adminIdx) { this.adminIdx = adminIdx; }

    public Integer getRoomIdx() { return roomIdx; }
    public void setRoomIdx(Integer roomIdx) { this.roomIdx = roomIdx; }

    public Integer getResvNum() { return resvNum; }
    public void setResvNum(Integer resvNum) { this.resvNum = resvNum; }

    public LocalDate getResvCheckIn() { return resvCheckIn; }
    public void setResvCheckIn(LocalDate resvCheckIn) { this.resvCheckIn = resvCheckIn; }

    public LocalDate getResvCheckOut() { return resvCheckOut; }
    public void setResvCheckOut(LocalDate resvCheckOut) { this.resvCheckOut = resvCheckOut; }

    public Integer getResvPrice() { return resvPrice; }
    public void setResvPrice(Integer resvPrice) { this.resvPrice = resvPrice; }

    public Integer getResvPeople() { return resvPeople; }
    public void setResvPeople(Integer resvPeople) { this.resvPeople = resvPeople; }

    public String getResvStatus() { return resvStatus; }
    public void setResvStatus(String resvStatus) { this.resvStatus = resvStatus; }

    public String getResvRoomPlan() { return resvRoomPlan; }
    public void setResvRoomPlan(String resvRoomPlan) { this.resvRoomPlan = resvRoomPlan; }

    public String getResvPayStatus() { return resvPayStatus; }
    public void setResvPayStatus(String resvPayStatus) { this.resvPayStatus = resvPayStatus; }

    public String getResvPayMethod() { return resvPayMethod; }
    public void setResvPayMethod(String resvPayMethod) { this.resvPayMethod = resvPayMethod; }
}
