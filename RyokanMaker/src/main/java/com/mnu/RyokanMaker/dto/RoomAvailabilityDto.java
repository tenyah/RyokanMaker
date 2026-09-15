package com.mnu.RyokanMaker.dto;

import java.util.List;

/** 캘린더 표의 한 행(row) : 객실 하나 + 날짜별 상태 목록 */
public class RoomAvailabilityDto {

    private String roomName;
    private List<DayStatusDto> days;

    public RoomAvailabilityDto() {
    }

    public RoomAvailabilityDto(String roomName, List<DayStatusDto> days) {
        this.roomName = roomName;
        this.days = days;
    }

    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }

    public List<DayStatusDto> getDays() { return days; }
    public void setDays(List<DayStatusDto> days) { this.days = days; }
}
