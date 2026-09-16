package com.mnu.ryokanmaker.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@RequestMapping("Admin")
public class AdminController {
	private static final Logger log =
			LoggerFactory.getLogger(AdminController.class);
	
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
	
}
