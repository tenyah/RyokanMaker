package com.mnu.ryokanmaker.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 이미지 여러 장을 CLOB 컬럼 하나에 저장하기 위한 공통 유틸.
 * [{"contentType":"image/png","data":"base64..."}, ...] 형태의 JSON 문자열로 변환한다.
 * 객실/식사/온천/시설 등 "이미지 여러 장 + CLOB 컬럼 하나" 패턴을 쓰는 모든 섹션에서 재사용.
 */
public final class ImageJsonUtil {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private ImageJsonUtil() {
	}

	/**
	 * 새로 올라온 파일이 하나도 없으면 null 리턴 (= 기존 이미지 유지 신호로 사용).
	 */
	public static String toJson(List<MultipartFile> files, int maxCount) throws IOException {
		if (files == null || files.isEmpty()) {
			return null;
		}
		List<Map<String, String>> images = new ArrayList<>();
		for (MultipartFile file : files) {
			if (file == null || file.isEmpty()) {
				continue;
			}
			String base64Data = Base64.getEncoder().encodeToString(file.getBytes());
			images.add(Map.of(
					"contentType", file.getContentType() == null ? "image/jpeg" : file.getContentType(),
					"data", base64Data
			));
			if (images.size() >= maxCount) {
				break;
			}
		}
		if (images.isEmpty()) {
			return null;
		}
		return OBJECT_MAPPER.writeValueAsString(images);
	}
}
