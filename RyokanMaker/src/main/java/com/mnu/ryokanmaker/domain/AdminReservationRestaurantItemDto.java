package com.mnu.ryokanmaker.domain;

import java.time.LocalDate;

import lombok.Data;
import lombok.NoArgsConstructor;

/** 관리자 - 예약 현황 상세 패널의 식사 예약 1행 (RESTAURANT_RESERVATION + RESTAURANT_COURSE 조인). */
@Data
@NoArgsConstructor
public class AdminReservationRestaurantItemDto {

    private String courseName;
    private LocalDate restaurantUseDate;
    private String restaurantTimeSlot;
    private Integer restaurantHeadcount;
    private String restaurantSidemenu;
}
