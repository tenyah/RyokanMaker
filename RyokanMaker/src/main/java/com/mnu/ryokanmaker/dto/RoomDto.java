package com.mnu.ryokanmaker.dto;

/**
 * ROOM 테이블 매핑 DTO (객실 마스터 정보)
 * PK : roomIdx / FK : adminIdx -> ADMIN.ADMIN_IDX
 */
public class RoomDto {

    private Integer roomIdx;
    private Integer adminIdx;
    private String roomName;
    private String roomLevel;
    private String roomInfo;
    private Integer roomPrice;
    private Integer roomPeople;
    private String roomImage;   // 이미지 경로 또는 파일명 (CLOB)
    private String roomSaleYN;
    private String roomMemo;

    public RoomDto() {
    }

    public RoomDto(Integer roomIdx, Integer adminIdx, String roomName, String roomLevel,
                    String roomInfo, Integer roomPrice, Integer roomPeople, String roomImage,
                    String roomSaleYN, String roomMemo) {
        this.roomIdx = roomIdx;
        this.adminIdx = adminIdx;
        this.roomName = roomName;
        this.roomLevel = roomLevel;
        this.roomInfo = roomInfo;
        this.roomPrice = roomPrice;
        this.roomPeople = roomPeople;
        this.roomImage = roomImage;
        this.roomSaleYN = roomSaleYN;
        this.roomMemo = roomMemo;
    }

    public Integer getRoomIdx() { return roomIdx; }
    public void setRoomIdx(Integer roomIdx) { this.roomIdx = roomIdx; }

    public Integer getAdminIdx() { return adminIdx; }
    public void setAdminIdx(Integer adminIdx) { this.adminIdx = adminIdx; }

    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }

    public String getRoomLevel() { return roomLevel; }
    public void setRoomLevel(String roomLevel) { this.roomLevel = roomLevel; }

    public String getRoomInfo() { return roomInfo; }
    public void setRoomInfo(String roomInfo) { this.roomInfo = roomInfo; }

    public Integer getRoomPrice() { return roomPrice; }
    public void setRoomPrice(Integer roomPrice) { this.roomPrice = roomPrice; }

    public Integer getRoomPeople() { return roomPeople; }
    public void setRoomPeople(Integer roomPeople) { this.roomPeople = roomPeople; }

    public String getRoomImage() { return roomImage; }
    public void setRoomImage(String roomImage) { this.roomImage = roomImage; }

    public String getRoomSaleYN() { return roomSaleYN; }
    public void setRoomSaleYN(String roomSaleYN) { this.roomSaleYN = roomSaleYN; }

    public String getRoomMemo() { return roomMemo; }
    public void setRoomMemo(String roomMemo) { this.roomMemo = roomMemo; }
}
