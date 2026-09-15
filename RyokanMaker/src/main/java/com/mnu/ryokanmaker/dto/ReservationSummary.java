package com.mnu.ryokanmaker.dto;

import java.time.LocalDate;

/** 결제 화면(payment.html) 우측 사이드바에 표시되는 예약 요약 정보 */
public class ReservationSummary {

    private String planBadge;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private int nights;
    private int roomCount;
    private int adultCount;
    private int childCount;
    private String roomPlanName;
    private String roomPlanDescription;
    private int roomFee;
    private int mealFee;
    private int totalAmount;
    private int cancelPolicyDays;

    public ReservationSummary() {
    }

    public ReservationSummary(String planBadge, LocalDate checkInDate, LocalDate checkOutDate,
                               int nights, int roomCount, int adultCount, int childCount,
                               String roomPlanName, String roomPlanDescription,
                               int roomFee, int mealFee, int totalAmount,
                               int cancelPolicyDays) {
        this.planBadge = planBadge;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.nights = nights;
        this.roomCount = roomCount;
        this.adultCount = adultCount;
        this.childCount = childCount;
        this.roomPlanName = roomPlanName;
        this.roomPlanDescription = roomPlanDescription;
        this.roomFee = roomFee;
        this.mealFee = mealFee;
        this.totalAmount = totalAmount;
        this.cancelPolicyDays = cancelPolicyDays;
    }

    public String getPlanBadge() { return planBadge; }
    public void setPlanBadge(String planBadge) { this.planBadge = planBadge; }

    public LocalDate getCheckInDate() { return checkInDate; }
    public void setCheckInDate(LocalDate checkInDate) { this.checkInDate = checkInDate; }

    public LocalDate getCheckOutDate() { return checkOutDate; }
    public void setCheckOutDate(LocalDate checkOutDate) { this.checkOutDate = checkOutDate; }

    public int getNights() { return nights; }
    public void setNights(int nights) { this.nights = nights; }

    public int getRoomCount() { return roomCount; }
    public void setRoomCount(int roomCount) { this.roomCount = roomCount; }

    public int getAdultCount() { return adultCount; }
    public void setAdultCount(int adultCount) { this.adultCount = adultCount; }

    public int getChildCount() { return childCount; }
    public void setChildCount(int childCount) { this.childCount = childCount; }

    public String getRoomPlanName() { return roomPlanName; }
    public void setRoomPlanName(String roomPlanName) { this.roomPlanName = roomPlanName; }

    public String getRoomPlanDescription() { return roomPlanDescription; }
    public void setRoomPlanDescription(String roomPlanDescription) { this.roomPlanDescription = roomPlanDescription; }

    public int getRoomFee() { return roomFee; }
    public void setRoomFee(int roomFee) { this.roomFee = roomFee; }

    public int getMealFee() { return mealFee; }
    public void setMealFee(int mealFee) { this.mealFee = mealFee; }

    public int getTotalAmount() { return totalAmount; }
    public void setTotalAmount(int totalAmount) { this.totalAmount = totalAmount; }

    public int getCancelPolicyDays() { return cancelPolicyDays; }
    public void setCancelPolicyDays(int cancelPolicyDays) { this.cancelPolicyDays = cancelPolicyDays; }
}
