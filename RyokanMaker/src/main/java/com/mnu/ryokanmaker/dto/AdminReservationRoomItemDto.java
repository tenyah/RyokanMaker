package com.mnu.ryokanmaker.dto;

import java.time.LocalDate;

/** 관리자 - 예약 현황 상세 패널의 객실 예약 1행 (ROOM_RESERVATION + ROOM + PLAN 조인). */
public class AdminReservationRoomItemDto {

    private String roomName;
    private String planName;
    private LocalDate resvCheckIn;
    private LocalDate resvCheckOut;
    private Integer resvPeople;
    private Integer resvPrice;

    public AdminReservationRoomItemDto() {
    }

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
