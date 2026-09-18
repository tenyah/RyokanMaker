package com.mnu.ryokanmaker.service;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.mnu.ryokanmaker.dto.AdminDto;
import com.mnu.ryokanmaker.mapper.AdminMapper;
import com.mnu.ryokanmaker.util.ImageJsonUtil;
import com.mnu.ryokanmaker.util.UserSHA256;

@Service
public class AdminService {

	private static final int MAX_MAIN_IMAGES = 10;

	@Autowired
	private AdminMapper adminMapper;

	/**
	 * PW_RESET_YN에 따라 비밀번호 비교 방식을 다르게 처리한다.
	 * 'N'(초기 비밀번호, 아직 변경 안 함) : DB에 평문으로 저장돼 있으므로 입력값을 그대로 비교.
	 * 'Y'(비밀번호 변경 완료) : DB에 SHA-256 해시로 저장돼 있으므로 입력값을 해시해서 비교.
	 * 아이디가 존재하지 않거나 비밀번호가 일치하지 않으면 null을 반환한다.
	 */
	public AdminDto adminLogin(AdminDto adminDto) {
		AdminDto found = adminMapper.selectByAdminId(adminDto.getAdminId());
		if (found == null) {
			return null;
		}

		String inputPassword = adminDto.getAdminPassword();
		boolean matches = "N".equals(found.getPwResetYn())
				? found.getAdminPassword().equals(inputPassword)
				: found.getAdminPassword().equals(UserSHA256.getSHA256(inputPassword));

		return matches ? found : null;
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
	 * 비밀번호 변경 (최초 로그인 강제 변경 포함). 세션에 있는 loginAdmin의 현재 해시와
	 * 입력한 현재 비밀번호의 해시를 비교해서 일치할 때만 변경하고,
	 * 변경 성공 시 PW_RESET_YN을 'Y'로 같이 갱신해서 초기 비밀번호 상태를 해제한다.
	 * 현재 비밀번호가 틀리면 false를 반환한다.
	 */
	public boolean resetPassword(AdminDto loginAdmin, String currentPassword, String newPassword) {
		if (!loginAdmin.getAdminPassword().equals(UserSHA256.getSHA256(currentPassword))) {
			return false;
		}

		AdminDto adminDto = new AdminDto();
		adminDto.setAdminIdx(loginAdmin.getAdminIdx());
		adminDto.setAdminPassword(UserSHA256.getSHA256(newPassword));
		adminMapper.updatePassword(adminDto);
		return true;
	}
}
