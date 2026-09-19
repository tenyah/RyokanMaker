package com.mnu.ryokanmaker.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.mnu.ryokanmaker.dto.AdminDto;
import com.mnu.ryokanmaker.dto.MemberDto;
import com.mnu.ryokanmaker.service.AdminService;
import com.mnu.ryokanmaker.service.InquiryService;

import jakarta.servlet.http.HttpSession;

/**
 * 관리자 화면 사이드바의 "문의 관리" 미답변 건수 배지, 헤더의 로그인 관리자/회원 정보처럼 여러
 * 페이지에 공통으로 필요한 값을 여기서 한 번만 계산해 모든 뷰의 모델에 넣어준다.
 * 최신 Thymeleaf(Spring 6)는 템플릿에서 #session을 직접 쓸 수 없으므로, 세션에 있는
 * 로그인 정보도 세션 대신 모델 속성("admin", "loginMember")으로 뷰에 전달한다.
 */
@ControllerAdvice
public class GlobalModelAdvice {

	private static final Logger log = LoggerFactory.getLogger(GlobalModelAdvice.class);

	// 사이트가 단일 료칸(清流庵) 기준이라 공개 화면(헤더/푸터)의 로고·상호·연락처도 이 관리자 소유 데이터로 고정 조회
	private static final int MAIN_ADMIN_IDX = 1;

	@Autowired
	private InquiryService inquiryService;

	@Autowired
	private AdminService adminService;

	@ModelAttribute("admin")
	public AdminDto admin(HttpSession session) {
		return (AdminDto) session.getAttribute("admin");
	}

	/**
	 * 공개 화면 헤더/푸터에 쓰는 료칸 기본정보(로고, 상호, 주소, 연락처, 이메일).
	 * 로그인 여부와 무관하게 모든 방문자에게 보여야 하므로 세션의 "admin"과 별도로 DB에서 조회한다.
	 */
	@ModelAttribute("siteInfo")
	public AdminDto siteInfo() {
		try {
			return adminService.getRyokanInfo(MAIN_ADMIN_IDX);
		} catch (Exception e) {
			log.warn("헤더/푸터용 료칸 기본정보 조회 실패 - 기본 텍스트로 표시합니다.", e);
			return null;
		}
	}

	@ModelAttribute("loginMember")
	public MemberDto loginMember(HttpSession session) {
		return (MemberDto) session.getAttribute("loginMember");
	}

	@ModelAttribute("pendingInquiryCount")
	public int pendingInquiryCount(HttpSession session) {
		AdminDto loginAdmin = (AdminDto) session.getAttribute("admin");
		if (loginAdmin == null) {
			return 0;
		}
		return inquiryService.countByStatus(loginAdmin.getAdminIdx(), "답변대기");
	}
}
