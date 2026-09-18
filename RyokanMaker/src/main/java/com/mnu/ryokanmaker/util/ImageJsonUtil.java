package com.mnu.ryokanmaker.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 이미지 여러 장을 실제 파일로 저장하고, 웹에서 접근 가능한 경로 목록을 CLOB 컬럼 하나에 저장하기 위한 공통 유틸.
 * 파일은 src/main/resources/static/uploads/{category}/ 아래에 저장되고,
 * DB에는 ["/uploads/{category}/xxx.jpg", ...] 형태의 JSON 배열 문자열(웹 경로)만 저장한다.
 * 객실/식사/온천/시설 등 "이미지 여러 장 + CLOB 컬럼 하나" 패턴을 쓰는 모든 섹션에서 재사용.
 */
public final class ImageJsonUtil {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	// static 리소스 루트. classpath가 아니라 소스 트리에 직접 쓴다 (개발 중 재빌드해도 남아있도록).
	private static final Path UPLOAD_ROOT = Paths.get("src/main/resources/static/uploads");

	private ImageJsonUtil() {
	}

	/**
	 * 새로 올라온 파일이 하나도 없으면 null 리턴 (= 기존 이미지 유지 신호로 사용).
	 * category : 파일을 구분해서 저장할 하위 폴더명 (예: "room", "onsen", "logo")
	 */
	public static String toJson(List<MultipartFile> files, int maxCount, String category) throws IOException {
		if (files == null || files.isEmpty()) {
			return null;
		}

		Path targetDir = UPLOAD_ROOT.resolve(category);
		Files.createDirectories(targetDir);

		List<String> paths = new ArrayList<>();
		for (MultipartFile file : files) {
			if (file == null || file.isEmpty()) {
				continue;
			}
			String savedFileName = UUID.randomUUID() + extensionOf(file);
			Path targetPath = targetDir.resolve(savedFileName);
			try (InputStream in = file.getInputStream()) {
				Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
			}
			paths.add("/uploads/" + category + "/" + savedFileName);
			if (paths.size() >= maxCount) {
				break;
			}
		}
		if (paths.isEmpty()) {
			return null;
		}
		return OBJECT_MAPPER.writeValueAsString(paths);
	}

	private static String extensionOf(MultipartFile file) {
		String originalName = file.getOriginalFilename();
		if (originalName == null) {
			return "";
		}
		int dotIndex = originalName.lastIndexOf('.');
		return dotIndex >= 0 ? originalName.substring(dotIndex) : "";
	}

	/**
	 * toJson()이 만든 경로 배열 JSON 문자열을 다시 List<String>으로 파싱.
	 * null/빈 문자열/파싱 실패 시 빈 리스트를 반환한다 (화면에서 널 체크 없이 바로 반복문 돌릴 수 있도록).
	 */
	public static List<String> parsePaths(String imageJson) {
		if (imageJson == null || imageJson.isEmpty()) {
			return Collections.emptyList();
		}
		try {
			return OBJECT_MAPPER.readValue(imageJson, OBJECT_MAPPER.getTypeFactory()
					.constructCollectionType(List.class, String.class));
		} catch (IOException e) {
			return Collections.emptyList();
		}
	}

	/**
	 * 목록 화면 썸네일용으로 첫 번째 이미지 경로만 뽑는다. 없으면 null.
	 */
	public static String firstPath(String imageJson) {
		List<String> paths = parsePaths(imageJson);
		return paths.isEmpty() ? null : paths.get(0);
	}
}
