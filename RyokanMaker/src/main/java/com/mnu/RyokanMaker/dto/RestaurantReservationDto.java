package com.mnu.RyokanMaker.dto;

import java.time.LocalDate;

/**
 * RESTAURANT_RESERVATION 테이블 매핑 DTO (식사 플랜 예약)
 * PK : restaurantFacilityIdx
 * FK : userMail -> MEMBER.UserMail, adminIdx -> ADMIN.AdminIdx, resvNum -> RESERVATION.Resv_num
 */
public class RestaurantReservationDto {

    private Integer restaurantFacilityIdx;
    private String userMail;
    private Integer adminIdx;
    private String restaurantType;
    private LocalDate restaurantUseDate;
    private String restaurantTimeSlot;
    private Integer restaurantHeadcount;
    private String restaurantSidemenu;
    private Integer resvNum;

    public RestaurantReservationDto() {
    }

    public RestaurantReservationDto(Integer restaurantFacilityIdx, String userMail, Integer adminIdx,
                                     String restaurantType, LocalDate restaurantUseDate, String restaurantTimeSlot,
                                     Integer restaurantHeadcount, String restaurantSidemenu, Integer resvNum) {
        this.restaurantFacilityIdx = restaurantFacilityIdx;
        this.userMail = userMail;
        this.adminIdx = adminIdx;
        this.restaurantType = restaurantType;
        this.restaurantUseDate = restaurantUseDate;
        this.restaurantTimeSlot = restaurantTimeSlot;
        this.restaurantHeadcount = restaurantHeadcount;
        this.restaurantSidemenu = restaurantSidemenu;
        this.resvNum = resvNum;
    }

    public Integer getRestaurantFacilityIdx() { return restaurantFacilityIdx; }
    public void setRestaurantFacilityIdx(Integer restaurantFacilityIdx) { this.restaurantFacilityIdx = restaurantFacilityIdx; }

    public String getUserMail() { return userMail; }
    public void setUserMail(String userMail) { this.userMail = userMail; }

    public Integer getAdminIdx() { return adminIdx; }
    public void setAdminIdx(Integer adminIdx) { this.adminIdx = adminIdx; }

    public String getRestaurantType() { return restaurantType; }
    public void setRestaurantType(String restaurantType) { this.restaurantType = restaurantType; }

    public LocalDate getRestaurantUseDate() { return restaurantUseDate; }
    public void setRestaurantUseDate(LocalDate restaurantUseDate) { this.restaurantUseDate = restaurantUseDate; }

    public String getRestaurantTimeSlot() { return restaurantTimeSlot; }
    public void setRestaurantTimeSlot(String restaurantTimeSlot) { this.restaurantTimeSlot = restaurantTimeSlot; }

    public Integer getRestaurantHeadcount() { return restaurantHeadcount; }
    public void setRestaurantHeadcount(Integer restaurantHeadcount) { this.restaurantHeadcount = restaurantHeadcount; }

    public String getRestaurantSidemenu() { return restaurantSidemenu; }
    public void setRestaurantSidemenu(String restaurantSidemenu) { this.restaurantSidemenu = restaurantSidemenu; }

    public Integer getResvNum() { return resvNum; }
    public void setResvNum(Integer resvNum) { this.resvNum = resvNum; }
}
