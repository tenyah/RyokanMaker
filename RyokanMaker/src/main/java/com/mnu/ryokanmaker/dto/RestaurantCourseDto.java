package com.mnu.ryokanmaker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * RESTAURANT_COURSE 테이블 매핑 DTO (식사 코스 마스터 정보)
 * PK : restaurantCourseIdx / FK : adminIdx -> ADMIN.ADMIN_IDX
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantCourseDto {

    private Integer restaurantCourseIdx;
    private String restaurantCourseName;
    private String restaurantCourseImage;
    private String restaurantCourseInfo;
    private Integer adminIdx;
    private String restaurantSaleYN;
    private String restaurantCourseMemo;
}
