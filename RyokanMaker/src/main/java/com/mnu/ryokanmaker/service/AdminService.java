package com.mnu.ryokanmaker.service;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.mnu.ryokanmaker.dto.AdminDto;
import com.mnu.ryokanmaker.mapper.AdminMapper;
import com.mnu.ryokanmaker.util.ImageJsonUtil;
import com.mnu.ryokanmaker.util.PasswordUtil;

@Service
public class AdminService {

	private static final int MAX_MAIN_IMAGES = 10;

	@Autowired
	private AdminMapper adminMapper;

	/**
	 * 로그인 : 아이디/비밀번호가 맞으면 관리자 정보를, 아니면 null을 반환.
	 * PW_RESET_YN='N'(가입 직후 초기 상태)이면 DB에 평문으로 저장되어 있어 평문 비교,
	 * 'Y'(비밀번호 변경 완료 후)이면 DB에 sha256 해시로 저장되어 있어 해시 비교.
	 */
	public AdminDto authenticate(String adminId, String adminPassword) {
		AdminDto admin = adminMapper.selectByAdminId(adminId);
		if (admin == null) {
			return null;
		}
		boolean matched = "Y".equals(admin.getPwResetYn())
				? admin.getAdminPassword().equals(PasswordUtil.sha256(adminPassword))
				: admin.getAdminPassword().equals(adminPassword);
		if (!matched) {
			return null;
		}
		return admin;
	}

	/**
	 * 여관 기본정보 수정 (인덱스 화면 입력). 1 관리자 = 1 여관이라 등록/삭제 없이 수정만 있음.
	 * logoFile : 로고 1장 (선택) - 안 올리면 기존 로고 유지
	 * ryokanImageFiles : 메인화면 슬라이드 이미지 최대 10장 (선택) - 안 올리면 기존 이미지 유지
	 */
	public void updateRyokanInfo(AdminDto adminDto, MultipartFile logoFile, List<MultipartFile> ryokanImageFiles) throws IOException {

		if (logoFile != null && !logoFile.isEmpty()) {
			String logoJson = ImageJsonUtil.toJson(List.of(logoFile), 1, "logo");
			adminDto.setRyokanLogo(logoJson);
		}

		String imagesJson = ImageJsonUtil.toJson(ryokanImageFiles, MAX_MAIN_IMAGES, "ryokan");
		if (imagesJson != null) {
			adminDto.setRyokanImage(imagesJson);
		}

		adminMapper.updateRyokanInfo(adminDto);
	}

	/**
	 * 교통안내(RYOKAN_ACCESS) 단독 수정. 인덱스 화면 본체(여관명/전화/메일/위치/이미지) 폼과 별도라
	 * 다른 필드를 건드리지 않도록 전용 update를 사용.
	 */
	public void updateRyokanAccess(AdminDto adminDto) {
		adminMapper.updateRyokanAccess(adminDto);
	}

	/**
	 * 비밀번호 변경 : 현재 비밀번호가 맞으면 새 비밀번호로 바꾸고 true, 아니면 false.
	 * 이 경로는 PW_RESET_YN='N'(가입 직후=평문 저장) 상태에서 호출되므로 현재 비밀번호는 평문으로 비교하고,
	 * 변경 후에는 PW_RESET_YN이 'Y'(해시 저장)로 바뀌므로 새 비밀번호는 sha256 해시로 저장한다.
	 */
	public boolean changePassword(Integer adminIdx, String currentPassword, String newPassword) {
		AdminDto admin = adminMapper.selectByAdminIdx(adminIdx);
		if (admin == null || !admin.getAdminPassword().equals(currentPassword)) {
			return false;
		}
		adminMapper.updatePassword(adminIdx, PasswordUtil.sha256(newPassword));
		return true;
	}
}
