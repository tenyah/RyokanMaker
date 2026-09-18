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

import com.mnu.ryokanmaker.domain.AdminDto;
import com.mnu.ryokanmaker.domain.AdminPlanDto;
import com.mnu.ryokanmaker.domain.AdminReservationDetailDto;
import com.mnu.ryokanmaker.domain.AdminReservationListItemDto;
import com.mnu.ryokanmaker.domain.FacilityDto;
import com.mnu.ryokanmaker.domain.InquiryDto;
import com.mnu.ryokanmaker.domain.NoticeDto;
import com.mnu.ryokanmaker.domain.OnsenDto;
import com.mnu.ryokanmaker.domain.RestaurantCourseDto;
import com.mnu.ryokanmaker.domain.RoomDto;
import com.mnu.ryokanmaker.service.AdminReservationService;
import com.mnu.ryokanmaker.service.AdminService;
import com.mnu.ryokanmaker.service.FacilityService;
import com.mnu.ryokanmaker.service.InquiryService;
import com.mnu.ryokanmaker.service.NoticeService;
import com.mnu.ryokanmaker.service.OnsenService;
import com.mnu.ryokanmaker.service.PlanService;
import com.mnu.ryokanmaker.service.RestaurantCourseService;
import com.mnu.ryokanmaker.service.RoomService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("Admin")
public class AdminController {
	private static final Logger log =
			LoggerFactory.getLogger(AdminController.class);

	private final AdminReservationService adminReservationService;

	public AdminController(AdminReservationService adminReservationService) {
		this.adminReservationService = adminReservationService;
	}

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

	@Autowired
	private PlanService planService;

	@Autowired
	private InquiryService inquiryService;

	private AdminDto currentAdmin(HttpSession session) {
		return (AdminDto) session.getAttribute("admin");
	}

	@GetMapping("admin_info_register")
	public String adminInfoRegister(HttpSession session, Model model) {
		AdminDto loginAdmin = currentAdmin(session);
		if (loginAdmin == null) {
			return "redirect:/Admin/admin_login";
		}
		model.addAttribute("roomList", roomService.getRoomList(loginAdmin.getAdminIdx()));
		model.addAttribute("courseList", restaurantCourseService.getCourseList(loginAdmin.getAdminIdx()));
		model.addAttribute("onsenList", onsenService.getOnsenList(loginAdmin.getAdminIdx()));
		model.addAttribute("planList", planService.getPlanList(loginAdmin.getAdminIdx()));
		model.addAttribute("facilityList", facilityService.getFacilityList(loginAdmin.getAdminIdx()));
		model.addAttribute("noticeList", noticeService.getNoticeList(loginAdmin.getAdminIdx()));
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
	public String adminInquiry(@RequestParam(value = "idx", required = false) Integer idx,
			@RequestParam(value = "status", required = false) String status,
			HttpSession session, Model model) {
		AdminDto loginAdmin = currentAdmin(session);
		if (loginAdmin == null) {
			return "redirect:/Admin/admin_login";
		}

		// Thymeleaf가 @{...(status=${nullValue})}를 렌더링할 때 빈 문자열("")로 넘어오는 경우가 있어 정규화
		if (status != null && status.isEmpty()) {
			status = null;
		}

		Integer adminIdx = loginAdmin.getAdminIdx();
		List<InquiryDto> inquiryList = inquiryService.getInquiryList(adminIdx, status);
		model.addAttribute("inquiryList", inquiryList);
		model.addAttribute("statusFilter", status);
		model.addAttribute("totalCount", inquiryService.countTotal(adminIdx));
		model.addAttribute("pendingCount", inquiryService.countByStatus(adminIdx, "답변대기"));
		model.addAttribute("answeredCount", inquiryService.countByStatus(adminIdx, "답변완료"));

		Integer selectedIdx = idx != null ? idx : (inquiryList.isEmpty() ? null : inquiryList.get(0).getInquiryIdx());
		if (selectedIdx != null) {
			model.addAttribute("selectedInquiry", inquiryService.getInquiry(selectedIdx, adminIdx));
		}

		return "Admin/admin_inquiry";
	}

	/**
	 * 문의 답변 등록. 답변 내용을 저장하면서 상태를 '답변완료'로 갱신한다.
	 */
	@PostMapping("inquiry_answer")
	public String inquiryAnswer(@RequestParam("inquiryIdx") Integer inquiryIdx,
			@RequestParam("inquiryAnswerContent") String inquiryAnswerContent,
			HttpSession session) {
		AdminDto loginAdmin = currentAdmin(session);
		if (loginAdmin == null) {
			return "redirect:/Admin/admin_login";
		}

		inquiryService.answerInquiry(inquiryIdx, loginAdmin.getAdminIdx(), inquiryAnswerContent);

		return "redirect:/Admin/admin_inquiry?idx=" + inquiryIdx;
	}

	/** 예약 현황 화면: 왼쪽 예약 목록 + 첫 번째 예약의 상세 패널을 함께 조회. */
	@GetMapping("reservation_status")
	public String reservationStatus(HttpSession session, Model model) {
		AdminDto loginAdmin = currentAdmin(session);
		if (loginAdmin == null) {
			return "redirect:/Admin/admin_login";
		}

		List<AdminReservationListItemDto> reservationList =
				adminReservationService.getReservationList(loginAdmin.getAdminIdx());
		model.addAttribute("reservationList", reservationList);

		if (!reservationList.isEmpty()) {
			Integer firstResvNum = reservationList.get(0).getResvNum();
			AdminReservationDetailDto detail = adminReservationService.getReservationDetail(firstResvNum);
			model.addAttribute("detail", detail);
		}

		return "Admin/admin_reservation";
	}

	@GetMapping("admin_login")
	public String adminLoginForm(HttpSession session) {
		if (currentAdmin(session) == null) {
			return "Admin/admin_login";
		}
		return "redirect:/Admin/admin_info_register";
	}

	@PostMapping("admin_login")
	public String adminLogin(@RequestParam String adminId,
			@RequestParam String adminPassword,
			HttpSession session, Model model) {
		AdminDto admin = adminService.authenticate(adminId, adminPassword);
		if (admin == null) {
			model.addAttribute("error", "아이디 또는 비밀번호가 일치하지 않습니다.");
			return "Admin/admin_login";
		}
		admin.setAdminPassword(null);
		session.setAttribute("admin", admin);
		session.setMaxInactiveInterval(60 * 20);

		if (!"Y".equals(admin.getPwResetYn())) {
			return "redirect:/Admin/admin_pwreset";
		}
		return "redirect:/Admin/admin_info_register";
	}

	@GetMapping("admin_pwreset")
	public String adminPwresetForm(HttpSession session, Model model) {
		AdminDto loginAdmin = currentAdmin(session);
		if (loginAdmin == null) {
			return "redirect:/Admin/admin_login";
		}
		model.addAttribute("admin", loginAdmin);
		return "Admin/admin_pwreset";
	}

	/**
	 * 비밀번호 변경. 현재 비밀번호가 일치할 때만 변경하며, 성공 시 PW_RESET_YN이 'Y'로 갱신되어
	 * 초기 비밀번호 상태가 해제된다(이후 로그인은 해시 비교).
	 */
	@PostMapping("password_reset")
	public String passwordReset(@RequestParam("currentPassword") String currentPassword,
			@RequestParam("newPassword") String newPassword,
			@RequestParam("newPasswordConfirm") String newPasswordConfirm,
			HttpSession session, Model model) {

		AdminDto loginAdmin = currentAdmin(session);
		if (loginAdmin == null) {
			return "redirect:/Admin/admin_login";
		}

		if (!newPassword.equals(newPasswordConfirm)) {
			model.addAttribute("admin", loginAdmin);
			model.addAttribute("error", "새 비밀번호가 일치하지 않습니다.");
			return "Admin/admin_pwreset";
		}

		boolean success = adminService.changePassword(loginAdmin.getAdminIdx(), currentPassword, newPassword);
		if (!success) {
			model.addAttribute("admin", loginAdmin);
			model.addAttribute("error", "현재 비밀번호가 일치하지 않습니다.");
			return "Admin/admin_pwreset";
		}

		loginAdmin.setPwResetYn("Y");
		session.setAttribute("admin", loginAdmin);

		return "redirect:/Admin/admin_info_register";
	}

	/**
	 * 여관 기본정보(인덱스 화면) 수정. 1 관리자 = 1 여관이라 등록/삭제 없이 수정만 있음.
	 */
	@PostMapping("admin_info_save")
	public String adminInfoSave(AdminDto adminDto,
			@RequestParam(value = "ryokanLogoFile", required = false) MultipartFile ryokanLogoFile,
			@RequestParam(value = "ryokanImageFiles", required = false) List<MultipartFile> ryokanImageFiles,
			HttpSession session) throws IOException {

		AdminDto loginAdmin = currentAdmin(session);
		if (loginAdmin == null) {
			return "redirect:/Admin/admin_login";
		}

		adminDto.setAdminIdx(loginAdmin.getAdminIdx());
		adminService.updateRyokanInfo(adminDto, ryokanLogoFile, ryokanImageFiles);

		// 세션에 캐시된 관리자 정보도 같이 갱신 (안 하면 다시 로그인하기 전까지 화면에 옛날 값이 보임)
		loginAdmin.setRyokanName(adminDto.getRyokanName());
		loginAdmin.setRyokanTel(adminDto.getRyokanTel());
		loginAdmin.setAdminMail(adminDto.getAdminMail());
		loginAdmin.setAdminLoc(adminDto.getAdminLoc());
		if (adminDto.getRyokanLogo() != null) {
			loginAdmin.setRyokanLogo(adminDto.getRyokanLogo());
		}
		if (adminDto.getRyokanImage() != null) {
			loginAdmin.setRyokanImage(adminDto.getRyokanImage());
		}
		session.setAttribute("admin", loginAdmin);

		return "redirect:/Admin/admin_info_register#section-index";
	}

	/**
	 * 교통안내(RYOKAN_ACCESS) 단독 수정. 인덱스 화면 본체 폼과 별도로 제출됨.
	 */
	@PostMapping("admin_access_save")
	public String adminAccessSave(@RequestParam("ryokanAccess") String ryokanAccess, HttpSession session) {
		AdminDto loginAdmin = currentAdmin(session);
		if (loginAdmin == null) {
			return "redirect:/Admin/admin_login";
		}

		AdminDto adminDto = new AdminDto();
		adminDto.setAdminIdx(loginAdmin.getAdminIdx());
		adminDto.setRyokanAccess(ryokanAccess);
		adminService.updateRyokanAccess(adminDto);

		loginAdmin.setRyokanAccess(ryokanAccess);
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

		AdminDto loginAdmin = currentAdmin(session);
		if (loginAdmin == null) {
			return "redirect:/Admin/admin_login";
		}

		roomDto.setAdminIdx(loginAdmin.getAdminIdx());
		roomService.saveRoom(roomDto, roomImageFiles);

		return "redirect:/Admin/admin_info_register#section-room";
	}

	@PostMapping("room_delete")
	public String roomDelete(@RequestParam("roomIdx") Integer roomIdx, HttpSession session) {
		AdminDto loginAdmin = currentAdmin(session);
		if (loginAdmin == null) {
			return "redirect:/Admin/admin_login";
		}
		roomService.deleteRoom(roomIdx, loginAdmin.getAdminIdx());
		return "redirect:/Admin/admin_info_register#section-room";
	}

	/**
	 * 식사(코스) 등록/수정 (upsert) : courseDto.restaurantCourseIdx가 없으면 신규 등록, 있으면 수정
	 */
	@PostMapping("course_save")
	public String courseSave(RestaurantCourseDto courseDto,
			@RequestParam(value = "courseImageFiles", required = false) List<MultipartFile> courseImageFiles,
			HttpSession session) throws IOException {

		AdminDto loginAdmin = currentAdmin(session);
		if (loginAdmin == null) {
			return "redirect:/Admin/admin_login";
		}

		courseDto.setAdminIdx(loginAdmin.getAdminIdx());
		restaurantCourseService.saveCourse(courseDto, courseImageFiles);

		return "redirect:/Admin/admin_info_register#section-meal";
	}

	@PostMapping("course_delete")
	public String courseDelete(@RequestParam("restaurantCourseIdx") Integer restaurantCourseIdx, HttpSession session) {
		AdminDto loginAdmin = currentAdmin(session);
		if (loginAdmin == null) {
			return "redirect:/Admin/admin_login";
		}
		restaurantCourseService.deleteCourse(restaurantCourseIdx, loginAdmin.getAdminIdx());
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

		AdminDto loginAdmin = currentAdmin(session);
		if (loginAdmin == null) {
			return "redirect:/Admin/admin_login";
		}

		onsenDto.setAdminIdx(loginAdmin.getAdminIdx());
		onsenService.saveOnsen(onsenDto, onsenStartTime, onsenEndTime, onsenImageFiles);

		return "redirect:/Admin/admin_info_register#section-onsen";
	}

	@PostMapping("onsen_delete")
	public String onsenDelete(@RequestParam("onsenIdx") Integer onsenIdx, HttpSession session) {
		AdminDto loginAdmin = currentAdmin(session);
		if (loginAdmin == null) {
			return "redirect:/Admin/admin_login";
		}
		onsenService.deleteOnsen(onsenIdx, loginAdmin.getAdminIdx());
		return "redirect:/Admin/admin_info_register#section-onsen";
	}

	/**
	 * 플랜 등록/수정 (upsert) : planDto.planIdx가 없으면 신규 등록, 있으면 수정
	 * 사용자 예약 화면에 실제 노출/판매되는 가격 단위. 객실 가격은 플랜 기준가 대비 추가요금 계산에만 쓰인다.
	 */
	@PostMapping("plan_save")
	public String planSave(AdminPlanDto planDto,
			@RequestParam(value = "planImageFiles", required = false) List<MultipartFile> planImageFiles,
			HttpSession session) throws IOException {

		AdminDto loginAdmin = currentAdmin(session);
		if (loginAdmin == null) {
			return "redirect:/Admin/admin_login";
		}

		planDto.setAdminIdx(loginAdmin.getAdminIdx());
		planService.savePlan(planDto, planImageFiles);

		return "redirect:/Admin/admin_info_register#section-plan";
	}

	@PostMapping("plan_delete")
	public String planDelete(@RequestParam("planIdx") Integer planIdx, HttpSession session) {
		AdminDto loginAdmin = currentAdmin(session);
		if (loginAdmin == null) {
			return "redirect:/Admin/admin_login";
		}
		planService.deletePlan(planIdx, loginAdmin.getAdminIdx());
		return "redirect:/Admin/admin_info_register#section-plan";
	}

	/**
	 * 시설 등록/수정 (upsert) : facilityDto.facilityIdx가 없으면 신규 등록, 있으면 수정
	 * 주의: FACILITY 테이블엔 노출여부(SALE_YN) 컬럼이 없어서 다른 섹션과 달리 노출 토글이 없음
	 */
	@PostMapping("facility_save")
	public String facilitySave(FacilityDto facilityDto,
			@RequestParam(value = "facilityImageFiles", required = false) List<MultipartFile> facilityImageFiles,
			HttpSession session) throws IOException {

		AdminDto loginAdmin = currentAdmin(session);
		if (loginAdmin == null) {
			return "redirect:/Admin/admin_login";
		}

		facilityDto.setAdminIdx(loginAdmin.getAdminIdx());
		facilityService.saveFacility(facilityDto, facilityImageFiles);

		return "redirect:/Admin/admin_info_register#section-facility";
	}

	@PostMapping("facility_delete")
	public String facilityDelete(@RequestParam("facilityIdx") Integer facilityIdx, HttpSession session) {
		AdminDto loginAdmin = currentAdmin(session);
		if (loginAdmin == null) {
			return "redirect:/Admin/admin_login";
		}
		facilityService.deleteFacility(facilityIdx, loginAdmin.getAdminIdx());
		return "redirect:/Admin/admin_info_register#section-facility";
	}
	/**
	 * 공지사항 등록/수정 (upsert) : noticeDto.noticeIdx가 없으면 신규 등록, 있으면 수정
	 * 이미지/노출여부 없음. 등록 시각(NOTICE_CREATED_AT)은 DB에서 SYSTIMESTAMP로 자동 기록.
	 */
	@PostMapping("notice_save")
	public String noticeSave(NoticeDto noticeDto, HttpSession session) {
		AdminDto loginAdmin = currentAdmin(session);
		if (loginAdmin == null) {
			return "redirect:/Admin/admin_login";
		}

		noticeDto.setAdminIdx(loginAdmin.getAdminIdx());
		noticeService.saveNotice(noticeDto);

		return "redirect:/Admin/admin_info_register#section-notice";
	}

	@PostMapping("notice_delete")
	public String noticeDelete(@RequestParam("noticeIdx") Integer noticeIdx, HttpSession session) {
		AdminDto loginAdmin = currentAdmin(session);
		if (loginAdmin == null) {
			return "redirect:/Admin/admin_login";
		}
		noticeService.deleteNotice(noticeIdx, loginAdmin.getAdminIdx());
		return "redirect:/Admin/admin_info_register#section-notice";
	}

	@GetMapping("admin_logout")
	public String adminLogout(HttpSession session) {
		session.invalidate();
		return "redirect:/Admin/admin_login";
	}
}
