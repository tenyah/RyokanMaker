package com.mnu.ryokanmaker.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 관리자 - 일자별 매출조회 화면의 거래 1건 (ROOM_RESERVATION 기준, 체크인 날짜 기준 조회).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RevenueTxnDto {

    private Integer resvNum;
    private LocalDate resvCheckIn;
    private String userNickname;
    private String roomName;
    private String planName;
    private String resvPayMethod;
    private String resvPayStatus;
    private Integer resvPrice;
}
