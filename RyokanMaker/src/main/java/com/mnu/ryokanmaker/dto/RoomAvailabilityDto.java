package com.mnu.ryokanmaker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 검색한 체크인~체크아웃 전체 기간 기준, 객실 하나의 예약 가능 여부 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomAvailabilityDto {

    private Integer roomIdx;
    private String roomName;
    private Integer roomPeople;
    private Integer roomPrice;
    private Integer extraCharge;
    private boolean available;
}
