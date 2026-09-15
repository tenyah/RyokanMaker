package com.mnu.RyokanMaker.domain;

import lombok.Data;

@Data
public class RoomDTO {
    private int roomIdx;
    private int adminIdx;
    private String roomName;
    private String roomLevel;
    private String roomInfo;
    private int roomPrice;
    private int roomPeople;
    private String roomimage;
    private String roomsaleyn;
    private String roomMemo;

}
