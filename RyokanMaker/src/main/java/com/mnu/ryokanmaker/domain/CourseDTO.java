package com.mnu.ryokanmaker.domain;
import lombok.Data;

@Data
public class CourseDTO {
    private Long restaurantCourseIdx;
    private String restaurantCourseName;
    private String restaurantCourseImage;
    private String restaurantCourseInfo;
    private Long adminIdx;
    private String restaurantCourseSaleYN;
}