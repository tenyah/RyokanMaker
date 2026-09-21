package com.mnu.ryokanmaker.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ROOM_RESERVATION 테이블 매핑 DTO (객실 예약 상세 - 체크인/아웃, 플랜 등)
 * PK : roomResvNum
 * FK : userMail -> MEMBER.USER_MAIL, adminIdx -> ADMIN.ADMIN_IDX,
 *      roomIdx -> ROOM.ROOM_IDX, resvNum -> RESERVATION.RESV_NUM
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomReservationDto {

    private Integer roomResvNum;
    private String userMail;
    private Integer adminIdx;
    private Integer roomIdx;
    private Integer resvNum;
    private Integer planIdx;
    private LocalDate resvCheckIn;
    private LocalDate resvCheckOut;
    private Integer resvPrice;
    private Integer resvPeople;
    private String resvStatus;
    private String resvPayStatus;
    private String resvPayMethod;
}
