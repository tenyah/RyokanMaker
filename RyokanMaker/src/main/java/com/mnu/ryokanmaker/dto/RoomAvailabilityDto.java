package com.mnu.ryokanmaker.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 캘린더 표의 한 행(row) : 객실 하나 + 날짜별 상태 목록 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomAvailabilityDto {

    private String roomName;
    private List<DayStatusDto> days;
}
