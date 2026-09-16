package com.mnu.ryokanmaker.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * 관리자 - 예약 현황 상세 패널(admin_reservation.html 우측)에 필요한 전체 데이터.
 * RESERVATION + MEMBER 조인 결과와, 하위 객실/식사/온천 예약 목록을 담는다.
 */
public class AdminReservationDetailDto {

    private Integer resvNum;
    private LocalDate resvDay;
    private String resvStatus;

    // 예약자 정보 (MEMBER)
    private String userNickname;
    private String userMail;
    private String userTel;
    private String userCountry;
    private String resvArrivalTime;

    // 예약 정보 (RESERVATION)
    private Integer resvPeople;
    private Integer resvPrice;
    private String resvPayStatus;
    private String resvPayMethod;
    private String resvRequest;

    private List<AdminReservationRoomItemDto> rooms;
    private List<AdminReservationRestaurantItemDto> restaurants;
    private List<AdminReservationOnsenItemDto> onsens;

    public AdminReservationDetailDto() {
    }

    public Integer getResvNum() { return resvNum; }
    public void setResvNum(Integer resvNum) { this.resvNum = resvNum; }

    public LocalDate getResvDay() { return resvDay; }
    public void setResvDay(LocalDate resvDay) { this.resvDay = resvDay; }

    public String getResvStatus() { return resvStatus; }
    public void setResvStatus(String resvStatus) { this.resvStatus = resvStatus; }

    public String getUserNickname() { return userNickname; }
    public void setUserNickname(String userNickname) { this.userNickname = userNickname; }

    public String getUserMail() { return userMail; }
    public void setUserMail(String userMail) { this.userMail = userMail; }

    public String getUserTel() { return userTel; }
    public void setUserTel(String userTel) { this.userTel = userTel; }

    public String getUserCountry() { return userCountry; }
    public void setUserCountry(String userCountry) { this.userCountry = userCountry; }

    public String getResvArrivalTime() { return resvArrivalTime; }
    public void setResvArrivalTime(String resvArrivalTime) { this.resvArrivalTime = resvArrivalTime; }

    public Integer getResvPeople() { return resvPeople; }
    public void setResvPeople(Integer resvPeople) { this.resvPeople = resvPeople; }

    public Integer getResvPrice() { return resvPrice; }
    public void setResvPrice(Integer resvPrice) { this.resvPrice = resvPrice; }

    public String getResvPayStatus() { return resvPayStatus; }
    public void setResvPayStatus(String resvPayStatus) { this.resvPayStatus = resvPayStatus; }

    public String getResvPayMethod() { return resvPayMethod; }
    public void setResvPayMethod(String resvPayMethod) { this.resvPayMethod = resvPayMethod; }

    public String getResvRequest() { return resvRequest; }
    public void setResvRequest(String resvRequest) { this.resvRequest = resvRequest; }

    public List<AdminReservationRoomItemDto> getRooms() { return rooms; }
    public void setRooms(List<AdminReservationRoomItemDto> rooms) { this.rooms = rooms; }

    public List<AdminReservationRestaurantItemDto> getRestaurants() { return restaurants; }
    public void setRestaurants(List<AdminReservationRestaurantItemDto> restaurants) { this.restaurants = restaurants; }

    public List<AdminReservationOnsenItemDto> getOnsens() { return onsens; }
    public void setOnsens(List<AdminReservationOnsenItemDto> onsens) { this.onsens = onsens; }
}
