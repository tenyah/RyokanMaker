package com.mnu.ryokanmaker.dto;

import java.util.List;

import lombok.Data;

import com.mnu.ryokanmaker.util.ImageJsonUtil;

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
    private String roomImage;     // ROOM_IMAGE (CLOB) - 이미지 경로 JSON 배열 문자열 저장
    private String roomSaleYn;
    private String roomMemo;      // ROOM_MEMO (비고)

    /** 목록 화면 썸네일용. roomImage(경로 JSON 배열)에서 첫 번째 이미지 경로만 뽑는다. */
    public String getThumbnailUrl() {
        return ImageJsonUtil.firstPath(roomImage);
    }

    /** 메인 페이지 캐러셀용. roomImage(경로 JSON 배열)의 전체 이미지 경로 목록. */
    public List<String> getImageUrls() {
        return ImageJsonUtil.parsePaths(roomImage);
    }
}
