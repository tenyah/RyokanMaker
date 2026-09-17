package com.mnu.ryokanmaker.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.mnu.ryokanmaker.domain.AdminDTO;
import com.mnu.ryokanmaker.service.InquiryService;

import jakarta.servlet.http.HttpSession;

/**
 * 관리자 화면 사이드바의 "문의 관리" 미답변 건수 배지처럼, 여러 관리자 페이지에 공통으로
 * 필요한 값을 여기서 한 번만 계산해 모든 뷰의 모델에 넣어준다.
 */
@ControllerAdvice(basePackages = "com.mnu.ryokanmaker.controller")
public class GlobalModelAdvice {

	@Autowired
	private InquiryService inquiryService;

	@ModelAttribute("pendingInquiryCount")
	public int pendingInquiryCount(HttpSession session) {
		AdminDTO loginAdmin = (AdminDTO) session.getAttribute("admin");
		if (loginAdmin == null) {
			return 0;
		}
		return inquiryService.countByStatus(loginAdmin.getAdmin_idx(), "답변대기");
	}
}
