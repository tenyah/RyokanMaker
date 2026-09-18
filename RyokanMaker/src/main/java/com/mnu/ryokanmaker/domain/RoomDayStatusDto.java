package com.mnu.ryokanmaker.domain;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 객실 현황(room_status.html) 캘린더 한 칸 : 특정 객실의 특정 날짜 예약 가능 여부. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomDayStatusDto {

    private LocalDate day;
    private boolean booked;
}
