package com.mnu.RyokanMaker.dto;

import java.time.LocalDate;

/** 검색 조건 바(체크인/체크아웃, 인원, 객실 수)에서 쓰는 값 */
public class SearchConditionDto {

    private LocalDate checkIn;
    private LocalDate checkOut;
    private int adultCount;
    private int childCount;
    private int roomCount;

    public SearchConditionDto() {
    }

    public SearchConditionDto(LocalDate checkIn, LocalDate checkOut, int adultCount, int childCount, int roomCount) {
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.adultCount = adultCount;
        this.childCount = childCount;
        this.roomCount = roomCount;
    }

    public LocalDate getCheckIn() { return checkIn; }
    public void setCheckIn(LocalDate checkIn) { this.checkIn = checkIn; }

    public LocalDate getCheckOut() { return checkOut; }
    public void setCheckOut(LocalDate checkOut) { this.checkOut = checkOut; }

    public int getAdultCount() { return adultCount; }
    public void setAdultCount(int adultCount) { this.adultCount = adultCount; }

    public int getChildCount() { return childCount; }
    public void setChildCount(int childCount) { this.childCount = childCount; }

    public int getRoomCount() { return roomCount; }
    public void setRoomCount(int roomCount) { this.roomCount = roomCount; }
}
