package com.mnu.RyokanMaker.dto;

import java.time.LocalDate;

/** 캘린더 표의 칸 하나(특정 객실 x 특정 날짜)의 상태 */
public class DayStatusDto {

    public static final String OK = "OK";     // 예약 가능
    public static final String ASK = "ASK";   // 전화/메일 문의
    public static final String NO = "NO";     // 마감

    private LocalDate date;
    private String status;   // OK / ASK / NO
    private int price;

    public DayStatusDto() {
    }

    public DayStatusDto(LocalDate date, String status, int price) {
        this.date = date;
        this.status = status;
        this.price = price;
    }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }
}
