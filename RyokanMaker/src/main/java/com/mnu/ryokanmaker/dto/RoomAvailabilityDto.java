package com.mnu.ryokanmaker.dto;

/** 검색한 체크인~체크아웃 전체 기간 기준, 객실 하나의 예약 가능 여부 */
public class RoomAvailabilityDto {

    private Long roomIdx;
    private String roomName;
    private Long roomPeople;
    private Long roomPrice;
    private Long extraCharge;
    private boolean available;

    public RoomAvailabilityDto() {
    }

    public RoomAvailabilityDto(Long roomIdx, String roomName, Long roomPeople, Long roomPrice,
                                Long extraCharge, boolean available) {
        this.roomIdx = roomIdx;
        this.roomName = roomName;
        this.roomPeople = roomPeople;
        this.roomPrice = roomPrice;
        this.extraCharge = extraCharge;
        this.available = available;
    }

    public Long getRoomIdx() { return roomIdx; }
    public void setRoomIdx(Long roomIdx) { this.roomIdx = roomIdx; }

    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }

    public Long getRoomPeople() { return roomPeople; }
    public void setRoomPeople(Long roomPeople) { this.roomPeople = roomPeople; }

    public Long getRoomPrice() { return roomPrice; }
    public void setRoomPrice(Long roomPrice) { this.roomPrice = roomPrice; }

    public Long getExtraCharge() { return extraCharge; }
    public void setExtraCharge(Long extraCharge) { this.extraCharge = extraCharge; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
}
