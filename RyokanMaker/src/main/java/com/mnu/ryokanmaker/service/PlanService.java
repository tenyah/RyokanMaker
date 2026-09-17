package com.mnu.ryokanmaker.service;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.mnu.ryokanmaker.domain.PlanDto;
import com.mnu.ryokanmaker.mapper.PlanMapper;
import com.mnu.ryokanmaker.util.ImageJsonUtil;

@Service
public class PlanService {

	private static final int MAX_IMAGES = 10;

	@Autowired
	private PlanMapper planMapper;

	public List<PlanDto> getPlanList(Integer adminIdx) {
		return planMapper.selectPlansByAdmin(adminIdx);
	}

	/**
	 * planIdx가 없으면(0 또는 null) 신규 등록, 있으면 수정으로 처리 (upsert)
	 * 수정 시 새 이미지를 올리지 않으면 기존 이미지를 그대로 유지함.
	 */
	public void savePlan(PlanDto planDto, List<MultipartFile> planImageFiles) throws IOException {

		if (planDto.getPlanIncludesMeal() == null || planDto.getPlanIncludesMeal().isEmpty()) {
			planDto.setPlanIncludesMeal("N");
		}
		if (planDto.getPlanIncludesOnsen() == null || planDto.getPlanIncludesOnsen().isEmpty()) {
			planDto.setPlanIncludesOnsen("N");
		}

		String imageJson = ImageJsonUtil.toJson(planImageFiles, MAX_IMAGES, "plan");
		if (imageJson != null) {
			planDto.setPlanImage(imageJson);
		}

		boolean isUpdate = planDto.getPlanIdx() != null && planDto.getPlanIdx() > 0;
		if (isUpdate) {
			planMapper.updatePlan(planDto);
		} else {
			planMapper.insertPlan(planDto);
		}
	}

	public void deletePlan(Integer planIdx, Integer adminIdx) {
		planMapper.deletePlan(planIdx, adminIdx);
	}
}
