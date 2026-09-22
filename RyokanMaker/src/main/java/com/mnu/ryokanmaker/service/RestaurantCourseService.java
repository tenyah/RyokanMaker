package com.mnu.ryokanmaker.service;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.mnu.ryokanmaker.domain.RestaurantCourseDto;
import com.mnu.ryokanmaker.mapper.RestaurantCourseMapper;
import com.mnu.ryokanmaker.util.ImageJsonUtil;

@Service
public class RestaurantCourseService {

	private static final int MAX_IMAGES = 10;

	@Autowired
	private RestaurantCourseMapper restaurantCourseMapper;

	public List<RestaurantCourseDto> getCourseList(Integer adminIdx) {
		return restaurantCourseMapper.selectCoursesByAdmin(adminIdx);
	}

	/**
	 * restaurantCourseIdx가 없으면(0 또는 null) 신규 등록, 있으면 수정으로 처리 (upsert)
	 * 수정 시 새 이미지를 올리지 않으면 기존 이미지를 그대로 유지함.
	 */
	public void saveCourse(RestaurantCourseDto courseDto, List<MultipartFile> courseImageFiles) throws IOException {

		if (courseDto.getRestaurantSaleYn() == null || courseDto.getRestaurantSaleYn().isEmpty()) {
			courseDto.setRestaurantSaleYn("N");
		}

		if (courseDto.getRestaurantCoursePrice() == null || courseDto.getRestaurantCoursePrice() < 0) {
			courseDto.setRestaurantCoursePrice(0);
		}

		String imageJson = ImageJsonUtil.toJson(courseImageFiles, MAX_IMAGES, "course");
		if (imageJson != null) {
			courseDto.setRestaurantCourseImage(imageJson);
		}

		boolean isUpdate = courseDto.getRestaurantCourseIdx() != null && courseDto.getRestaurantCourseIdx() > 0;
		if (isUpdate) {
			restaurantCourseMapper.updateCourse(courseDto);
		} else {
			restaurantCourseMapper.insertCourse(courseDto);
		}
	}

	public void deleteCourse(Integer restaurantCourseIdx, Integer adminIdx) {
		restaurantCourseMapper.deleteCourse(restaurantCourseIdx, adminIdx);
	}
}
