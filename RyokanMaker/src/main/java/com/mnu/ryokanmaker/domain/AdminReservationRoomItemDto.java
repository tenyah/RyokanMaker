package com.mnu.ryokanmaker.domain;

import java.time.LocalDate;

import lombok.Data;
import lombok.NoArgsConstructor;

/** 관리자 - 예약 현황 상세 패널의 객실 예약 1행 (ROOM_RESERVATION + ROOM + PLAN 조인). */
@Data
@NoArgsConstructor
public class AdminReservationRoomItemDto {

    private String roomName;
    private String planName;
    private LocalDate resvCheckIn;
    private LocalDate resvCheckOut;
    private Integer resvPeople;
    private Integer resvPrice;
}
