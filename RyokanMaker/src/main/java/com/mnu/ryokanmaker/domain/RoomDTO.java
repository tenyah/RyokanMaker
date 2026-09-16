package com.mnu.ryokanmaker.domain;
import lombok.Data;

@Data
public class RoomDTO {
    private Long roomIdx;
    private Long adminIdx;
    private String roomName;
    private String roomLevel;
    private String roomInfo;
    private Long roomPrice;
    private Long roomPeople;
    private String roomImage;
    private String roomMemo;
    private String roomSaleYN;
}