package com.mnu.ryokanmaker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ROOM 테이블 매핑 DTO (객실 마스터 정보)
 * PK : roomIdx / FK : adminIdx -> ADMIN.ADMIN_IDX
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
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
}
