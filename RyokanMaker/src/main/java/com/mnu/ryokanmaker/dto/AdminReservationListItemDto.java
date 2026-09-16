package com.mnu.ryokanmaker.dto;

import java.time.LocalDate;

/**
 * 관리자 - 예약 현황 화면(admin_reservation.html) 왼쪽 목록 카드 1행.
 * RESERVATION을 기준으로 MEMBER, 대표 ROOM_RESERVATION(+ROOM, PLAN)을 조인한 조회 전용 DTO.
 */
public class AdminReservationListItemDto {

    private Integer resvNum;
    private String userNickname;
    private String resvStatus;
    private String roomName;
    private String planName;
    private LocalDate resvCheckIn;
    private LocalDate resvCheckOut;
    private Integer resvPeople;
    private Integer resvPrice;

    public AdminReservationListItemDto() {
    }

    public Integer getResvNum() { return resvNum; }
    public void setResvNum(Integer resvNum) { this.resvNum = resvNum; }

    public String getUserNickname() { return userNickname; }
    public void setUserNickname(String userNickname) { this.userNickname = userNickname; }

    public String getResvStatus() { return resvStatus; }
    public void setResvStatus(String resvStatus) { this.resvStatus = resvStatus; }

    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }

    public String getPlanName() { return planName; }
    public void setPlanName(String planName) { this.planName = planName; }

    public LocalDate getResvCheckIn() { return resvCheckIn; }
    public void setResvCheckIn(LocalDate resvCheckIn) { this.resvCheckIn = resvCheckIn; }

    public LocalDate getResvCheckOut() { return resvCheckOut; }
    public void setResvCheckOut(LocalDate resvCheckOut) { this.resvCheckOut = resvCheckOut; }

    public Integer getResvPeople() { return resvPeople; }
    public void setResvPeople(Integer resvPeople) { this.resvPeople = resvPeople; }

    public Integer getResvPrice() { return resvPrice; }
    public void setResvPrice(Integer resvPrice) { this.resvPrice = resvPrice; }
}
