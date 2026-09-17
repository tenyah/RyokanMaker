package com.mnu.ryokanmaker.domain;

import lombok.Data;

import com.mnu.ryokanmaker.util.ImageJsonUtil;

/**
 * FACILITY 테이블 매핑 DTO (시설 마스터 정보)
 * PK : facilityIdx (IDENTITY, 자동 채번) / FK : adminIdx -> ADMIN.ADMIN_IDX
 * 주의: 이 테이블엔 노출여부(SALE_YN) 컬럼이 없음 -> 화면에도 "사이트에 노출" 토글 없앰
 */
@Data
public class FacilityDto {

    private Integer facilityIdx;
    private Integer adminIdx;
    private String facilityName;
    private String facilityInfo;
    private String facilityImage;  // FACILITY_IMAGE (CLOB) - 이미지 경로 JSON 배열 문자열 저장
    private String facilityMemo;

    /** 목록 화면 썸네일용. facilityImage(경로 JSON 배열)에서 첫 번째 이미지 경로만 뽑는다. */
    public String getThumbnailUrl() {
        return ImageJsonUtil.firstPath(facilityImage);
    }
}
