package com.mnu.ryokanmaker.dto;

import lombok.Data;

import com.mnu.ryokanmaker.util.ImageJsonUtil;

/**
 * RESTAURANT_COURSE 테이블 매핑 DTO (식사 코스 마스터 정보)
 * PK : restaurantCourseIdx (IDENTITY, 자동 채번) / FK : adminIdx -> ADMIN.ADMIN_IDX
 */
@Data
public class RestaurantCourseDto {

    private Integer restaurantCourseIdx;
    private String restaurantCourseName;
    private String restaurantCourseImage;  // RESTAURANT_COURSE_IMAGE (CLOB) - 이미지 경로 JSON 배열 문자열 저장
    private String restaurantCourseInfo;
    private Integer adminIdx;
    private String restaurantSaleYn;       // RESTAURANT_SALE_YN, 기본값 'Y'
    private String restaurantCourseMemo;   // 비고

    /** 목록 화면 썸네일용. restaurantCourseImage(경로 JSON 배열)에서 첫 번째 이미지 경로만 뽑는다. */
    public String getThumbnailUrl() {
        return ImageJsonUtil.firstPath(restaurantCourseImage);
    }
}
