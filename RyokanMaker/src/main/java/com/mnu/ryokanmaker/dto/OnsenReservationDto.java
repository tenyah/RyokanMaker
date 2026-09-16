package com.mnu.ryokanmaker.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ONSEN_RESERVATION 테이블 매핑 DTO
 * PK : onsenFacilityIdx
 * FK : adminIdx -> ADMIN.ADMIN_IDX, userMail -> MEMBER.USER_MAIL, resvNum -> RESERVATION.RESV_NUM,
 *      onsenIdx -> ONSEN.ONSEN_IDX
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
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
}
