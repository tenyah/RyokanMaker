package com.mnu.ryokanmaker.dto;

/**
 * RESTAURANT_COURSE 테이블 매핑 DTO (식사 코스 마스터 정보)
 * PK : courseIdx / FK : adminIdx -> ADMIN.ADMIN_IDX
 */
public class RestaurantCourseDto {

    private Integer courseIdx;
    private String courseName;
    private String courseImage;
    private String courseInfo;
    private Integer adminIdx;
    private String courseSaleYN;
    private String courseMemo;

    public RestaurantCourseDto() {
    }

    public RestaurantCourseDto(Integer courseIdx, String courseName, String courseImage,
                                String courseInfo, Integer adminIdx, String courseSaleYN,
                                String courseMemo) {
        this.courseIdx = courseIdx;
        this.courseName = courseName;
        this.courseImage = courseImage;
        this.courseInfo = courseInfo;
        this.adminIdx = adminIdx;
        this.courseSaleYN = courseSaleYN;
        this.courseMemo = courseMemo;
    }

    public Integer getCourseIdx() { return courseIdx; }
    public void setCourseIdx(Integer courseIdx) { this.courseIdx = courseIdx; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public String getCourseImage() { return courseImage; }
    public void setCourseImage(String courseImage) { this.courseImage = courseImage; }

    public String getCourseInfo() { return courseInfo; }
    public void setCourseInfo(String courseInfo) { this.courseInfo = courseInfo; }

    public Integer getAdminIdx() { return adminIdx; }
    public void setAdminIdx(Integer adminIdx) { this.adminIdx = adminIdx; }

    public String getCourseSaleYN() { return courseSaleYN; }
    public void setCourseSaleYN(String courseSaleYN) { this.courseSaleYN = courseSaleYN; }

    public String getCourseMemo() { return courseMemo; }
    public void setCourseMemo(String courseMemo) { this.courseMemo = courseMemo; }
}
