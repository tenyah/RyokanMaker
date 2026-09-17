package com.mnu.ryokanmaker.domain;

import lombok.Data;

/**
 * ROOM 테이블 매핑 DTO (객실 마스터 정보)
 * PK : roomIdx (IDENTITY, 자동 채번) / FK : adminIdx -> ADMIN.ADMIN_IDX
 */
@Data
public class RoomDto {

    private Integer roomIdx;
    private Integer adminIdx;
    private String roomName;
    private String roomLevel;
    private String roomInfo;
    private Integer roomPrice;
    private Integer roomPeople;
    private String roomImage;     // ROOM_IMAGE (CLOB) - 이미지 base64 문자열 저장
    private String roomSaleYn;    // ROOM_SALE_YN, 기본값 'Y'
    private String roomMemo;      // ROOM_MEMO (비고)
}
