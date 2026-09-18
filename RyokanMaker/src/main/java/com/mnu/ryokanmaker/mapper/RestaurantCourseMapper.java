package com.mnu.ryokanmaker.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.mnu.ryokanmaker.dto.RestaurantCourseDto;

@Mapper
public interface RestaurantCourseMapper {
	public int insertCourse(RestaurantCourseDto courseDto);
	public List<RestaurantCourseDto> selectCoursesByAdmin(@Param("adminIdx") Integer adminIdx);
	public int updateCourse(RestaurantCourseDto courseDto);
	public int deleteCourse(@Param("restaurantCourseIdx") Integer restaurantCourseIdx, @Param("adminIdx") Integer adminIdx);
}
