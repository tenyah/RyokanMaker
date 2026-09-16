package com.mnu.ryokanmaker.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * RESTAURANT_RESERVATION 테이블 매핑 DTO (식사 플랜 예약)
 * PK : restaurantFacilityIdx
 * FK : userMail -> MEMBER.USER_MAIL, adminIdx -> ADMIN.ADMIN_IDX, resvNum -> RESERVATION.RESV_NUM,
 *      courseIdx -> RESTAURANT_COURSE.COURSE_IDX
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantReservationDto {

    private Integer restaurantFacilityIdx;
    private String userMail;
    private Integer adminIdx;
    private LocalDate restaurantUseDate;
    private String restaurantTimeSlot;
    private Integer restaurantHeadcount;
    private String restaurantSidemenu;
    private Integer resvNum;
    private Integer courseIdx;
}
