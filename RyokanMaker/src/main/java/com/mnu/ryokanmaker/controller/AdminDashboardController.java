package com.mnu.ryokanmaker.controller;

import java.time.LocalDate;
import java.time.YearMonth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.mnu.ryokanmaker.domain.AdminDto;
import com.mnu.ryokanmaker.service.DashboardService;
import com.mnu.ryokanmaker.service.RevenueService;

import jakarta.servlet.http.HttpSession;

/** 관리자 - 대시보드(오늘 운영 요약) 화면. */
@Controller
@RequestMapping("Admin")
public class AdminDashboardController {

	@Autowired
	private DashboardService dashboardService;

	@Autowired
	private RevenueService revenueService;

	private AdminDto currentAdmin(HttpSession session) {
		return (AdminDto) session.getAttribute("admin");
	}

	@GetMapping("dashboard")
	public String dashboard(HttpSession session, Model model) {
		AdminDto loginAdmin = currentAdmin(session);
		if (loginAdmin == null) {
			return "redirect:/Admin/admin_login";
		}

		Integer adminIdx = loginAdmin.getAdminIdx();
		model.addAttribute("stats", dashboardService.getStats(adminIdx));
		model.addAttribute("todayCheckIns", dashboardService.getTodayCheckIns(adminIdx));
		model.addAttribute("todayCheckOuts", dashboardService.getTodayCheckOuts(adminIdx));

		// 매출 및 예약 트렌드 : 매출현황(월매출조회) 화면과 동일한 차트를 이번 달 기준으로 그대로 재사용한다.
		YearMonth ym = YearMonth.now();
		LocalDate monthStart = ym.atDay(1);
		LocalDate monthEnd = ym.plusMonths(1).atDay(1);
		model.addAttribute("monthlyTrend", revenueService.getMonthlyTrend(adminIdx, ym.getYear()));
		model.addAttribute("roomShare", revenueService.getRoomShare(adminIdx, monthStart, monthEnd));
		model.addAttribute("planSettlement", revenueService.getPlanSettlement(adminIdx, monthStart, monthEnd));

		return "Admin/dashboard";
	}
}
