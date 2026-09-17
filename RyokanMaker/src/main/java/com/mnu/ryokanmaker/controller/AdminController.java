package com.mnu.ryokanmaker.controller;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.mnu.ryokanmaker.domain.AdminDTO;
import com.mnu.ryokanmaker.domain.FacilityDto;
import com.mnu.ryokanmaker.domain.NoticeDto;
import com.mnu.ryokanmaker.domain.OnsenDto;
import com.mnu.ryokanmaker.domain.RestaurantCourseDto;
import com.mnu.ryokanmaker.domain.RoomDto;
import com.mnu.ryokanmaker.service.AdminService;
import com.mnu.ryokanmaker.service.FacilityService;
import com.mnu.ryokanmaker.service.NoticeService;
import com.mnu.ryokanmaker.service.OnsenService;
import com.mnu.ryokanmaker.service.RestaurantCourseService;
import com.mnu.ryokanmaker.service.RoomService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;


@Controller
@RequestMapping("Admin")
public class AdminController {
	
	private static final Logger log =
			LoggerFactory.getLogger(AdminController.class);
	
	@Autowired
	private AdminService adminService;

	@Autowired
	private RoomService roomService;

	@Autowired
	private RestaurantCourseService restaurantCourseService;

	@Autowired
	private OnsenService onsenService;

	@Autowired
	private FacilityService facilityService;

	@Autowired
	private NoticeService noticeService;

	@GetMapping("admin_info_register")
	public String adminInfoRegister(HttpSession session, Model model) {
		AdminDTO loginAdmin = (AdminDTO) session.getAttribute("admin");
		if (loginAdmin == null) {
			return "redirect:/Admin/admin_login";
		}
		model.addAttribute("roomList", roomService.getRoomList(loginAdmin.getAdmin_idx()));
		model.addAttribute("courseList", restaurantCourseService.getCourseList(loginAdmin.getAdmin_idx()));
		model.addAttribute("onsenList", onsenService.getOnsenList(loginAdmin.getAdmin_idx()));
		model.addAttribute("facilityList", facilityService.getFacilityList(loginAdmin.getAdmin_idx()));
		model.addAttribute("noticeList", noticeService.getNoticeList(loginAdmin.getAdmin_idx()));
		return "Admin/admin_info_register";
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
	@GetMapping("admin_login")
	public String adminLogin(HttpSession session){
	    if (session.getAttribute("admin") == null)
	        return "Admin/admin_login";
	    else
	        return "redirect:/Admin/admin_info_register";
	}

	@PostMapping("admin_login")
	public String adminLoginPro(AdminDTO adminDTO, HttpServletRequest request, Model model){
	    AdminDTO admin = adminService.adminLogin(adminDTO);

	    if (admin == null) {
	        model.addAttribute("error", "아이디 또는 비밀번호가 일치하지 않습니다.");
	        return "Admin/admin_login";
	    }

	    request.getSession().setAttribute("admin", admin);
	    request.getSession().setMaxInactiveInterval(60 * 20);

	    if ("N".equals(admin.getPw_reset_yn())) {
	        return "redirect:/Admin/admin_pwreset";
	    }
	    return "redirect:/Admin/admin_info_register";
	}
	@GetMapping("admin_pwreset")
	public String adminPwreset(HttpSession session, Model model){
		AdminDTO loginAdmin = (AdminDTO) session.getAttribute("admin");
		if (loginAdmin == null) {
			return "redirect:/Admin/admin_login";
		}
		model.addAttribute("admin", loginAdmin);
		return "Admin/admin_pwreset";
	}

	/**
	 * 비밀번호 변경. 현재 비밀번호가 일치할 때만 변경하며, 성공 시 PW_RESET_YN이 'Y'로 갱신되어
	 * 초기 비밀번호 상태가 해제된다.
	 */
	@PostMapping("password_reset")
	public String passwordReset(@RequestParam("currentPassword") String currentPassword,
			@RequestParam("newPassword") String newPassword,
			@RequestParam("newPasswordConfirm") String newPasswordConfirm,
			HttpSession session, Model model) {

		AdminDTO loginAdmin = (AdminDTO) session.getAttribute("admin");
		if (loginAdmin == null) {
			return "redirect:/Admin/admin_login";
		}

		if (!newPassword.equals(newPasswordConfirm)) {
			model.addAttribute("admin", loginAdmin);
			model.addAttribute("error", "새 비밀번호가 일치하지 않습니다.");
			return "Admin/admin_pwreset";
		}

		boolean success = adminService.resetPassword(loginAdmin, currentPassword, newPassword);
		if (!success) {
			model.addAttribute("admin", loginAdmin);
			model.addAttribute("error", "현재 비밀번호가 일치하지 않습니다.");
			return "Admin/admin_pwreset";
		}

		loginAdmin.setPw_reset_yn("Y");
		session.setAttribute("admin", loginAdmin);

		return "redirect:/Admin/admin_info_register";
	}

	/**
	 * 여관 기본정보(인덱스 화면) 수정. 1 관리자 = 1 여관이라 등록/삭제 없이 수정만 있음.
	 */
	@PostMapping("admin_info_save")
	public String adminInfoSave(AdminDTO adminDto,
			@RequestParam(value = "ryokanLogoFile", required = false) MultipartFile ryokanLogoFile,
			@RequestParam(value = "ryokanImageFiles", required = false) List<MultipartFile> ryokanImageFiles,
			HttpSession session) throws IOException {

		AdminDTO loginAdmin = (AdminDTO) session.getAttribute("admin");
		if (loginAdmin == null) {
			return "redirect:/Admin/admin_login";
		}

		adminDto.setAdmin_idx(loginAdmin.getAdmin_idx());
		adminService.updateRyokanInfo(adminDto, ryokanLogoFile, ryokanImageFiles);

		// 세션에 캐시된 관리자 정보도 같이 갱신 (안 하면 다시 로그인하기 전까지 화면에 옛날 값이 보임)
		loginAdmin.setRyokan_name(adminDto.getRyokan_name());
		loginAdmin.setRyokan_tel(adminDto.getRyokan_tel());
		loginAdmin.setAdmin_mail(adminDto.getAdmin_mail());
		loginAdmin.setAdmin_loc(adminDto.getAdmin_loc());
		if (adminDto.getRyokan_logo() != null) {
			loginAdmin.setRyokan_logo(adminDto.getRyokan_logo());
		}
		if (adminDto.getRyokan_image() != null) {
			loginAdmin.setRyokan_image(adminDto.getRyokan_image());
		}
		session.setAttribute("admin", loginAdmin);

		return "redirect:/Admin/admin_info_register#section-index";
	}

	/**
	 * 교통안내(RYOKAN_ACCESS) 단독 수정. 인덱스 화면 본체 폼과 별도로 제출됨.
	 */
	@PostMapping("admin_access_save")
	public String adminAccessSave(@RequestParam("ryokanAccess") String ryokanAccess, HttpSession session) {
		AdminDTO loginAdmin = (AdminDTO) session.getAttribute("admin");
		if (loginAdmin == null) {
			return "redirect:/Admin/admin_login";
		}

		AdminDTO adminDto = new AdminDTO();
		adminDto.setAdmin_idx(loginAdmin.getAdmin_idx());
		adminDto.setRyokan_access(ryokanAccess);
		adminService.updateRyokanAccess(adminDto);

		loginAdmin.setRyokan_access(ryokanAccess);
		session.setAttribute("admin", loginAdmin);

		return "redirect:/Admin/admin_info_register#section-route";
	}

	/**
	 * 객실 등록/수정 (upsert) : roomDto.roomIdx가 없으면 신규 등록, 있으면 수정
	 */
	@PostMapping("room_save")
	public String roomSave(RoomDto roomDto,
			@RequestParam(value = "roomImageFiles", required = false) List<MultipartFile> roomImageFiles,
			HttpSession session) throws IOException {

		AdminDTO loginAdmin = (AdminDTO) session.getAttribute("admin");
		if (loginAdmin == null) {
			return "redirect:/Admin/admin_login";
		}

		roomDto.setAdminIdx(loginAdmin.getAdmin_idx());
		roomService.saveRoom(roomDto, roomImageFiles);

		return "redirect:/Admin/admin_info_register#section-room";
	}

	@PostMapping("room_delete")
	public String roomDelete(@RequestParam("roomIdx") Integer roomIdx, HttpSession session) {
		AdminDTO loginAdmin = (AdminDTO) session.getAttribute("admin");
		if (loginAdmin == null) {
			return "redirect:/Admin/admin_login";
		}
		roomService.deleteRoom(roomIdx, loginAdmin.getAdmin_idx());
		return "redirect:/Admin/admin_info_register#section-room";
	}

	/**
	 * 식사(코스) 등록/수정 (upsert) : courseDto.restaurantCourseIdx가 없으면 신규 등록, 있으면 수정
	 */
	@PostMapping("course_save")
	public String courseSave(RestaurantCourseDto courseDto,
			@RequestParam(value = "courseImageFiles", required = false) List<MultipartFile> courseImageFiles,
			HttpSession session) throws IOException {

		AdminDTO loginAdmin = (AdminDTO) session.getAttribute("admin");
		if (loginAdmin == null) {
			return "redirect:/Admin/admin_login";
		}

		courseDto.setAdminIdx(loginAdmin.getAdmin_idx());
		restaurantCourseService.saveCourse(courseDto, courseImageFiles);

		return "redirect:/Admin/admin_info_register#section-meal";
	}

	@PostMapping("course_delete")
	public String courseDelete(@RequestParam("restaurantCourseIdx") Integer restaurantCourseIdx, HttpSession session) {
		AdminDTO loginAdmin = (AdminDTO) session.getAttribute("admin");
		if (loginAdmin == null) {
			return "redirect:/Admin/admin_login";
		}
		restaurantCourseService.deleteCourse(restaurantCourseIdx, loginAdmin.getAdmin_idx());
		return "redirect:/Admin/admin_info_register#section-meal";
	}

	/**
	 * 온천 등록/수정 (upsert) : onsenDto.onsenIdx가 없으면 신규 등록, 있으면 수정
	 * 이용 시작/종료 시간은 onsenStartTime/onsenEndTime으로 따로 받아서 ONSEN_HOUR 문자열로 합쳐 저장
	 */
	@PostMapping("onsen_save")
	public String onsenSave(OnsenDto onsenDto,
			@RequestParam(value = "onsenStartTime", required = false) String onsenStartTime,
			@RequestParam(value = "onsenEndTime", required = false) String onsenEndTime,
			@RequestParam(value = "onsenImageFiles", required = false) List<MultipartFile> onsenImageFiles,
			HttpSession session) throws IOException {

		AdminDTO loginAdmin = (AdminDTO) session.getAttribute("admin");
		if (loginAdmin == null) {
			return "redirect:/Admin/admin_login";
		}

		onsenDto.setAdminIdx(loginAdmin.getAdmin_idx());
		onsenService.saveOnsen(onsenDto, onsenStartTime, onsenEndTime, onsenImageFiles);

		return "redirect:/Admin/admin_info_register#section-onsen";
	}

	@PostMapping("onsen_delete")
	public String onsenDelete(@RequestParam("onsenIdx") Integer onsenIdx, HttpSession session) {
		AdminDTO loginAdmin = (AdminDTO) session.getAttribute("admin");
		if (loginAdmin == null) {
			return "redirect:/Admin/admin_login";
		}
		onsenService.deleteOnsen(onsenIdx, loginAdmin.getAdmin_idx());
		return "redirect:/Admin/admin_info_register#section-onsen";
	}

	/**
	 * 시설 등록/수정 (upsert) : facilityDto.facilityIdx가 없으면 신규 등록, 있으면 수정
	 * 주의: FACILITY 테이블엔 노출여부(SALE_YN) 컬럼이 없어서 다른 섹션과 달리 노출 토글이 없음
	 */
	@PostMapping("facility_save")
	public String facilitySave(FacilityDto facilityDto,
			@RequestParam(value = "facilityImageFiles", required = false) List<MultipartFile> facilityImageFiles,
			HttpSession session) throws IOException {

		AdminDTO loginAdmin = (AdminDTO) session.getAttribute("admin");
		if (loginAdmin == null) {
			return "redirect:/Admin/admin_login";
		}

		facilityDto.setAdminIdx(loginAdmin.getAdmin_idx());
		facilityService.saveFacility(facilityDto, facilityImageFiles);

		return "redirect:/Admin/admin_info_register#section-facility";
	}

	@PostMapping("facility_delete")
	public String facilityDelete(@RequestParam("facilityIdx") Integer facilityIdx, HttpSession session) {
		AdminDTO loginAdmin = (AdminDTO) session.getAttribute("admin");
		if (loginAdmin == null) {
			return "redirect:/Admin/admin_login";
		}
		facilityService.deleteFacility(facilityIdx, loginAdmin.getAdmin_idx());
		return "redirect:/Admin/admin_info_register#section-facility";
	}
	/**
	 * 공지사항 등록/수정 (upsert) : noticeDto.noticeIdx가 없으면 신규 등록, 있으면 수정
	 * 이미지/노출여부 없음. 등록 시각(NOTICE_CREATED_AT)은 DB에서 SYSTIMESTAMP로 자동 기록.
	 */
	@PostMapping("notice_save")
	public String noticeSave(NoticeDto noticeDto, HttpSession session) {
		AdminDTO loginAdmin = (AdminDTO) session.getAttribute("admin");
		if (loginAdmin == null) {
			return "redirect:/Admin/admin_login";
		}

		noticeDto.setAdminIdx(loginAdmin.getAdmin_idx());
		noticeService.saveNotice(noticeDto);

		return "redirect:/Admin/admin_info_register#section-notice";
	}

	@PostMapping("notice_delete")
	public String noticeDelete(@RequestParam("noticeIdx") Integer noticeIdx, HttpSession session) {
		AdminDTO loginAdmin = (AdminDTO) session.getAttribute("admin");
		if (loginAdmin == null) {
			return "redirect:/Admin/admin_login";
		}
		noticeService.deleteNotice(noticeIdx, loginAdmin.getAdmin_idx());
		return "redirect:/Admin/admin_info_register#section-notice";
	}

	@GetMapping("admin_logout")
	public String adminLogout() {
		return"Admin/admin_login";
	}
}
