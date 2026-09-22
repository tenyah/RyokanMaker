package com.mnu.ryokanmaker.service;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.mnu.ryokanmaker.domain.FacilityDto;
import com.mnu.ryokanmaker.mapper.FacilityMapper;
import com.mnu.ryokanmaker.util.ImageJsonUtil;

@Service
public class FacilityService {

	private static final int MAX_IMAGES = 10;

	@Autowired
	private FacilityMapper facilityMapper;

	public List<FacilityDto> getFacilityList(Integer adminIdx) {
		return facilityMapper.selectFacilitiesByAdmin(adminIdx);
	}

	/**
	 * facilityIdx가 없으면(0 또는 null) 신규 등록, 있으면 수정으로 처리 (upsert)
	 * 수정 시 새 이미지를 올리지 않으면 기존 이미지를 그대로 유지함.
	 */
	public void saveFacility(FacilityDto facilityDto, List<MultipartFile> facilityImageFiles) throws IOException {

		String imageJson = ImageJsonUtil.toJson(facilityImageFiles, MAX_IMAGES, "facility");
		if (imageJson != null) {
			facilityDto.setFacilityImage(imageJson);
		}

		boolean isUpdate = facilityDto.getFacilityIdx() != null && facilityDto.getFacilityIdx() > 0;
		if (isUpdate) {
			facilityMapper.updateFacility(facilityDto);
		} else {
			facilityMapper.insertFacility(facilityDto);
		}
	}

	public void deleteFacility(Integer facilityIdx, Integer adminIdx) {
		facilityMapper.deleteFacility(facilityIdx, adminIdx);
	}
}
