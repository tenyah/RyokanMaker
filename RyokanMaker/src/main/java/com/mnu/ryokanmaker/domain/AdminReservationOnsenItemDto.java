package com.mnu.ryokanmaker.domain;

import java.time.LocalDate;

import lombok.Data;
import lombok.NoArgsConstructor;

/** 관리자 - 예약 현황 상세 패널의 온천 예약 1행 (ONSEN_RESERVATION + ONSEN 조인). */
@Data
@NoArgsConstructor
public class AdminReservationOnsenItemDto {

    private String onsenName;
    private LocalDate onsenUseDate;
    private String onsenTimeSlot;
    private Integer onsenHeadcount;
    private String onsenStatus;
}
