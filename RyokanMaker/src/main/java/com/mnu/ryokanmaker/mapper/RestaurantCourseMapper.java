package com.mnu.ryokanmaker.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.mnu.ryokanmaker.domain.RestaurantCourseDto;

@Mapper
public interface RestaurantCourseMapper {
	public int insertCourse(RestaurantCourseDto courseDto);
	public List<RestaurantCourseDto> selectCoursesByAdmin(@Param("adminIdx") Integer adminIdx);
	public int updateCourse(RestaurantCourseDto courseDto);
	public int deleteCourse(@Param("restaurantCourseIdx") Integer restaurantCourseIdx, @Param("adminIdx") Integer adminIdx);

	/** 판매 여부(RESTAURANT_SALE_YN)만 토글 */
	public int updateSaleYn(@Param("restaurantCourseIdx") Integer restaurantCourseIdx,
			@Param("adminIdx") Integer adminIdx, @Param("restaurantSaleYn") String restaurantSaleYn);

	/** 사용자 예약 화면(식사 코스 선택)에 노출할 판매중(SALE_YN='Y') 코스 전체 조회 */
	public List<RestaurantCourseDto> findAllOnSale();
}
