package com.mnu.ryokanmaker.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 객실 현황(room_status.html) 캘린더 한 행 : 객실 1개 + 조회 기간 날짜별 상태. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomStatusRowDto {

    private RoomDto room;
    private List<RoomDayStatusDto> days;
}
