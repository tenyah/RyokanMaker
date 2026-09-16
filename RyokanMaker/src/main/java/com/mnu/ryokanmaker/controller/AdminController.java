package com.mnu.ryokanmaker.controller;

import com.mnu.ryokanmaker.dto.AdminReservationDetailDto;
import com.mnu.ryokanmaker.dto.AdminReservationListItemDto;
import com.mnu.ryokanmaker.service.AdminReservationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;

import java.util.List;


@Controller
@RequestMapping("Admin")
public class AdminController {
	private static final Logger log =
			LoggerFactory.getLogger(AdminController.class);

	// TODO: 관리자 로그인/세션이 아직 없어 임시로 고정값 사용. 로그인 구현되면 세션의 AdminIdx로 교체.
	private static final Integer TEMP_ADMIN_IDX = 1;

	private final AdminReservationService adminReservationService;

	public AdminController(AdminReservationService adminReservationService) {
		this.adminReservationService = adminReservationService;
	}

	@GetMapping("admin_info_register")
	public String adminInfoRegister() {
		return"Admin/admin_info_register";
	}
	@GetMapping("plan_sales")
	public String planSales() {
		return"Admin/plan_sales";
	}
	@GetMapping("room_status")
	public String roomStatus() {
		return"Admin/room_status";
	}
	@GetMapping("admin_inquiry")
	public String adminInquiry() {
		return"Admin/admin_inquiry";
	}
	@GetMapping("admin_reservation")
	public String adminReservation() {
		return"Admin/admin_reservation";
	}

	/** 예약 현황 화면: 왼쪽 예약 목록 + 첫 번째 예약의 상세 패널을 함께 조회. */
	@GetMapping("reservation_status")
	public String reservationStatus(Model model) {
		List<AdminReservationListItemDto> reservationList =
				adminReservationService.getReservationList(TEMP_ADMIN_IDX);
		model.addAttribute("reservationList", reservationList);

		if (!reservationList.isEmpty()) {
			Integer firstResvNum = reservationList.get(0).getResvNum();
			AdminReservationDetailDto detail = adminReservationService.getReservationDetail(firstResvNum);
			model.addAttribute("detail", detail);
		}

		return "Admin/admin_reservation";
	}
	@GetMapping("admin_login")
	public String adminLogin(@RequestParam String adminId,
            @RequestParam String adminPassword,
            HttpSession session, Model model){
		return"Admin/admin_login";
	}
	@GetMapping("admin_pwreset")
	public String adminPwreset(){
		return"Admin/admin_pwreset";
	}
	@GetMapping("admin_logout")
	public String adminLogout() {
		return"Admin/admin_login";
	}
}
