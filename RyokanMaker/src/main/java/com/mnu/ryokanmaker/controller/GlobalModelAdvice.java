package com.mnu.ryokanmaker.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.mnu.ryokanmaker.domain.AdminDto;
import com.mnu.ryokanmaker.domain.MemberDto;
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

	@Autowired
	private InquiryService inquiryService;

	@ModelAttribute("admin")
	public AdminDto admin(HttpSession session) {
		return (AdminDto) session.getAttribute("admin");
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
