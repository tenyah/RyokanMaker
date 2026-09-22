package com.mnu.ryokanmaker.domain;

import java.time.LocalDate;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 관리자 - 예약 현황 화면(admin_reservation.html) 왼쪽 목록 카드 1행.
 * RESERVATION을 기준으로 MEMBER, 대표 ROOM_RESERVATION(+ROOM, PLAN)을 조인한 조회 전용 DTO.
 */
@Data
@NoArgsConstructor
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
}
