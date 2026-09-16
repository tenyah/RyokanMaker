package com.mnu.ryokanmaker.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 캘린더 표의 칸 하나(특정 객실 x 특정 날짜)의 상태 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DayStatusDto {

    public static final String OK = "OK";     // 예약 가능
    public static final String ASK = "ASK";   // 전화/메일 문의
    public static final String NO = "NO";     // 마감

    private LocalDate date;
    private String status;   // OK / ASK / NO
    private int price;
}
