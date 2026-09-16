package com.mnu.ryokanmaker.dto;

/**
 * RESTAURANT_COURSE 테이블 매핑 DTO (식사 코스 마스터 정보)
 * PK : restaurantCourseIdx / FK : adminIdx -> ADMIN.AdminIdx
 */
public class RestaurantCourseDto {

    private Integer restaurantCourseIdx;
    private String restaurantCourseName;
    private String restaurantCourseImage;
    private String restaurantCourseInfo;
    private Integer adminIdx;
    private String restaurantCourseSaleYN;
    private String restaurantCourseMemo;

    public RestaurantCourseDto() {
    }

    public RestaurantCourseDto(Integer restaurantCourseIdx, String restaurantCourseName, String restaurantCourseImage,
                                String restaurantCourseInfo, Integer adminIdx, String restaurantCourseSaleYN,
                                String restaurantCourseMemo) {
        this.restaurantCourseIdx = restaurantCourseIdx;
        this.restaurantCourseName = restaurantCourseName;
        this.restaurantCourseImage = restaurantCourseImage;
        this.restaurantCourseInfo = restaurantCourseInfo;
        this.adminIdx = adminIdx;
        this.restaurantCourseSaleYN = restaurantCourseSaleYN;
        this.restaurantCourseMemo = restaurantCourseMemo;
    }

    public Integer getRestaurantCourseIdx() { return restaurantCourseIdx; }
    public void setRestaurantCourseIdx(Integer restaurantCourseIdx) { this.restaurantCourseIdx = restaurantCourseIdx; }

    public String getRestaurantCourseName() { return restaurantCourseName; }
    public void setRestaurantCourseName(String restaurantCourseName) { this.restaurantCourseName = restaurantCourseName; }

    public String getRestaurantCourseImage() { return restaurantCourseImage; }
    public void setRestaurantCourseImage(String restaurantCourseImage) { this.restaurantCourseImage = restaurantCourseImage; }

    public String getRestaurantCourseInfo() { return restaurantCourseInfo; }
    public void setRestaurantCourseInfo(String restaurantCourseInfo) { this.restaurantCourseInfo = restaurantCourseInfo; }

    public Integer getAdminIdx() { return adminIdx; }
    public void setAdminIdx(Integer adminIdx) { this.adminIdx = adminIdx; }

    public String getRestaurantCourseSaleYN() { return restaurantCourseSaleYN; }
    public void setRestaurantCourseSaleYN(String restaurantCourseSaleYN) { this.restaurantCourseSaleYN = restaurantCourseSaleYN; }

    public String getRestaurantCourseMemo() { return restaurantCourseMemo; }
    public void setRestaurantCourseMemo(String restaurantCourseMemo) { this.restaurantCourseMemo = restaurantCourseMemo; }
}
