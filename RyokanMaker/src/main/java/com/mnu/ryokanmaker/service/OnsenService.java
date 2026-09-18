package com.mnu.ryokanmaker.service;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.mnu.ryokanmaker.domain.OnsenDto;
import com.mnu.ryokanmaker.mapper.OnsenMapper;
import com.mnu.ryokanmaker.util.ImageJsonUtil;

@Service
public class OnsenService {

	private static final int MAX_IMAGES = 10;

	@Autowired
	private OnsenMapper onsenMapper;

	public List<OnsenDto> getOnsenList(Integer adminIdx) {
		return onsenMapper.selectOnsensByAdmin(adminIdx);
	}

	/**
	 * onsenIdx가 없으면(0 또는 null) 신규 등록, 있으면 수정으로 처리 (upsert)
	 * startTime/endTime : 화면의 이용 시작/종료 시간(1시간 단위 드롭다운) -> ONSEN_HOUR 문자열로 합쳐서 저장.
	 *   둘 다 비어있으면 기존 값 유지(수정 시) / null로 저장(신규 등록 시).
	 * onsenImageFiles : 최대 10장, 파일로 저장 후 경로 배열을 JSON 문자열로 ONSEN_IMAGE(CLOB)에 저장.
	 * 수정 시 새 이미지를 올리지 않으면 기존 이미지를 그대로 유지함.
	 */
	public void saveOnsen(OnsenDto onsenDto, String startTime, String endTime, List<MultipartFile> onsenImageFiles) throws IOException {

		if (onsenDto.getOnsenSaleYn() == null || onsenDto.getOnsenSaleYn().isEmpty()) {
			onsenDto.setOnsenSaleYn("N");
		}

		boolean isUpdate = onsenDto.getOnsenIdx() != null && onsenDto.getOnsenIdx() > 0;

		// 시간을 둘 다 입력 안 했고 수정 모드라면 기존 시간을 건드리지 않도록 null로 둠 (매퍼가 컬럼을 세팅하긴 하지만,
		// 화면에서 항상 두 시간을 같이 보여주고 받기 때문에 보통은 항상 값이 들어옴)
		boolean hasStart = startTime != null && !startTime.isEmpty();
		boolean hasEnd = endTime != null && !endTime.isEmpty();
		if (hasStart || hasEnd) {
			onsenDto.setOnsenHourFromRange(startTime, endTime);
		}

		String imageJson = ImageJsonUtil.toJson(onsenImageFiles, MAX_IMAGES, "onsen");
		if (imageJson != null) {
			onsenDto.setOnsenImage(imageJson);
		}

		if (isUpdate) {
			onsenMapper.updateOnsen(onsenDto);
		} else {
			onsenMapper.insertOnsen(onsenDto);
		}
	}

	public void deleteOnsen(Integer onsenIdx, Integer adminIdx) {
		onsenMapper.deleteOnsen(onsenIdx, adminIdx);
	}
}
