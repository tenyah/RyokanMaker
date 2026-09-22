package com.mnu.ryokanmaker.domain;

import java.time.LocalDate;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 관리자 - 예약 현황 상세 패널(admin_reservation.html 우측)에 필요한 전체 데이터.
 * RESERVATION + MEMBER 조인 결과와, 하위 객실/식사/온천 예약 목록을 담는다.
 */
@Data
@NoArgsConstructor
public class AdminReservationDetailDto {

    private Integer resvNum;
    private LocalDate resvDay;
    private String resvStatus;

    // 예약자 정보 (MEMBER)
    private String userNickname;
    private String userMail;
    private String resvArrivalTime;
    // 예약자(실제 숙박객) — 예약한 회원(userMail/userNickname)과 다를 수 있다
    private String resvLastNameEn;
    private String resvFirstNameEn;
    private String resvLastNameJp;
    private String resvFirstNameJp;
    private String resvMail;
    private String resvCountry;  // COUNTRY_CODE.COUNTRY_NAME (FK)
    private String resvTel;

    // 예약 정보 (RESERVATION)
    private Integer resvPeople;
    private Integer resvPrice;
    private String resvPayStatus;
    private String resvPayMethod;
    private String resvRequest;

    private List<AdminReservationRoomItemDto> rooms;
    private List<AdminReservationRestaurantItemDto> restaurants;
    private List<AdminReservationOnsenItemDto> onsens;
}
