package com.mnu.ryokanmaker.service;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.mnu.ryokanmaker.dto.AdminDto;
import com.mnu.ryokanmaker.mapper.AdminMapper;
import com.mnu.ryokanmaker.util.ImageJsonUtil;
import com.mnu.ryokanmaker.util.PasswordUtil;
import com.mnu.ryokanmaker.util.SecretCipher;

@Service
public class AdminService {

	private static final Logger log = LoggerFactory.getLogger(AdminService.class);

	private static final int MAX_MAIN_IMAGES = 10;

	@Autowired
	private AdminMapper adminMapper;

	@Autowired
	private SecretCipher secretCipher;

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

	/** 관리자 인덱스로 단건 조회 (교통안내 등 공개 화면에서 여관 정보 표시용). */
	public AdminDto findByAdminIdx(int adminIdx) {
		return adminMapper.selectByAdminIdx(adminIdx);
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

	/** 여관 기본정보 조회 (메인 화면 히어로 캐러셀 등 비로그인 공개 화면에서 사용). */
	public AdminDto getRyokanInfo(int adminIdx) {
		return adminMapper.selectByAdminIdx(adminIdx);
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

	public AdminDto findByAdminIdx(Integer adminIdx) {
		return adminMapper.selectByAdminIdx(adminIdx);
	}

	/** 메일 앱 비밀번호를 암호화해서 저장할 수 있는 상태인지 (MAIL_SECRET_KEY 설정 여부) */
	public boolean isMailSecretAvailable() {
		return secretCipher.isAvailable();
	}

	/** 저장된 메일 앱 비밀번호가 있는지. 컬럼이 아직 없거나 DB 오류면 없는 것으로 본다. */
	public boolean hasMailPassword(Integer adminIdx) {
		try {
			String stored = adminMapper.selectMailPassword(adminIdx);
			return stored != null && !stored.isBlank();
		} catch (Exception e) {
			log.warn("메일 앱 비밀번호를 조회하지 못했습니다 (ADMIN.MAIL_APP_PASSWORD 컬럼 확인 필요): {}", e.getMessage());
			return false;
		}
	}

	/** 복호화한 메일 앱 비밀번호. 없거나 복호화할 수 없으면 null. 발송 직전에만 쓰고 화면에는 절대 내보내지 않는다. */
	public String getMailPassword(Integer adminIdx) {
		try {
			String stored = adminMapper.selectMailPassword(adminIdx);
			if (stored == null || stored.isBlank() || !secretCipher.isAvailable()) {
				return null;
			}
			return secretCipher.decrypt(stored);
		} catch (Exception e) {
			log.warn("메일 앱 비밀번호를 읽지 못했습니다", e);
			return null;
		}
	}

	/** 메일 앱 비밀번호 저장(암호화). 키가 없으면 IllegalStateException. */
	public void saveMailPassword(Integer adminIdx, String plainPassword) {
		adminMapper.updateMailPassword(adminIdx, secretCipher.encrypt(plainPassword.strip()));
	}

	public void clearMailPassword(Integer adminIdx) {
		adminMapper.updateMailPassword(adminIdx, null);
	}
}
