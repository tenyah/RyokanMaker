package com.mnu.ryokanmaker.dto;

import java.time.LocalDate;

/** 관리자 - 예약 현황 상세 패널의 식사 예약 1행 (RESTAURANT_RESERVATION + RESTAURANT_COURSE 조인). */
public class AdminReservationRestaurantItemDto {

    private String courseName;
    private LocalDate restaurantUseDate;
    private String restaurantTimeSlot;
    private Integer restaurantHeadcount;
    private String restaurantSidemenu;

    public AdminReservationRestaurantItemDto() {
    }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public LocalDate getRestaurantUseDate() { return restaurantUseDate; }
    public void setRestaurantUseDate(LocalDate restaurantUseDate) { this.restaurantUseDate = restaurantUseDate; }

    public String getRestaurantTimeSlot() { return restaurantTimeSlot; }
    public void setRestaurantTimeSlot(String restaurantTimeSlot) { this.restaurantTimeSlot = restaurantTimeSlot; }

    public Integer getRestaurantHeadcount() { return restaurantHeadcount; }
    public void setRestaurantHeadcount(Integer restaurantHeadcount) { this.restaurantHeadcount = restaurantHeadcount; }

    public String getRestaurantSidemenu() { return restaurantSidemenu; }
    public void setRestaurantSidemenu(String restaurantSidemenu) { this.restaurantSidemenu = restaurantSidemenu; }
}
