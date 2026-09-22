package com.mnu.ryokanmaker.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
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
import com.mnu.ryokanmaker.domain.PlanSalesRowDto;
import com.mnu.ryokanmaker.domain.RestaurantCourseDto;
import com.mnu.ryokanmaker.domain.RoomDto;
import com.mnu.ryokanmaker.domain.RoomStatusRowDto;
import com.mnu.ryokanmaker.service.AdminReservationService;
import com.mnu.ryokanmaker.service.AdminService;
import com.mnu.ryokanmaker.service.FacilityService;
import com.mnu.ryokanmaker.service.InquiryService;
import com.mnu.ryokanmaker.service.NoticeService;
import com.mnu.ryokanmaker.service.OnsenService;
import com.mnu.ryokanmaker.service.PlanSalesService;
import com.mnu.ryokanmaker.service.PlanService;
import com.mnu.ryokanmaker.service.RestaurantCourseService;
import com.mnu.ryokanmaker.service.RoomService;
import com.mnu.ryokanmaker.service.RoomStatusService;
import com.mnu.ryokanmaker.util.PageIndex;

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

	@Autowired
	private RoomStatusService roomStatusService;

	@Autowired
	private PlanSalesService planSalesService;

	private static final int STATUS_RANGE_DAYS = 7;

	private AdminDto currentAdmin(HttpSession session) {
		return (AdminDto) session.getAttribute("admin");
	}

	/** 객실/플랜은 admin_info_register 외에 room_status/plan_sales 화면에서도 CRUD 폼을 쓰므로,
	 *  제출한 화면으로 되돌아가도록 redirectTo를 받는다. 화이트리스트 밖 값은 무시한다(open redirect 방지). */
	private static final java.util.Set<String> ROOM_SAVE_REDIRECT_TARGETS = java.util.Set.of(
			"admin_info_register", "room_status");
	private static final java.util.Set<String> PLAN_SAVE_REDIRECT_TARGETS = java.util.Set.of(
			"admin_info_register", "plan_sales");

	private String roomRedirect(String redirectTo) {
		String target = ROOM_SAVE_REDIRECT_TARGETS.contains(redirectTo) ? redirectTo : "admin_info_register";
		return "admin_info_register".equals(target)
				? "redirect:/Admin/admin_info_register#section-room"
				: "redirect:/Admin/room_status";
	}

	private String planRedirect(String redirectTo) {
		String target = PLAN_SAVE_REDIRECT_TARGETS.contains(redirectTo) ? redirectTo : "admin_info_register";
		return "admin_info_register".equals(target)
				? "redirect:/Admin/admin_info_register#section-plan"
				: "redirect:/Admin/plan_sales";
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
	public String planSales(@RequestParam(value = "rangeStart", required = false)
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate rangeStart,
			HttpSession session, Model model) {
		AdminDto loginAdmin = currentAdmin(session);
		if (loginAdmin == null) {
			return "redirect:/Admin/admin_login";
		}
		if (rangeStart == null) {
			rangeStart = LocalDate.now();
		}

		List<PlanSalesRowDto> salesGrid = planSalesService.getSalesGrid(loginAdmin.getAdminIdx(), rangeStart, STATUS_RANGE_DAYS);
		model.addAttribute("planList", planService.getPlanList(loginAdmin.getAdminIdx()));
		model.addAttribute("onsenList", onsenService.getOnsenList(loginAdmin.getAdminIdx()));
		model.addAttribute("salesGrid", salesGrid);
		model.addAttribute("rangeStart", rangeStart);
		model.addAttribute("rangeEnd", rangeStart.plusDays(STATUS_RANGE_DAYS - 1));
		return "Admin/plan_sales";
	}

	@GetMapping("room_status")
	public String roomStatus(@RequestParam(value = "rangeStart", required = false)
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate rangeStart,
			HttpSession session, Model model) {
		AdminDto loginAdmin = currentAdmin(session);
		if (loginAdmin == null) {
			return "redirect:/Admin/admin_login";
		}
		if (rangeStart == null) {
			rangeStart = LocalDate.now();
		}

		List<RoomStatusRowDto> statusGrid = roomStatusService.getStatusGrid(loginAdmin.getAdminIdx(), rangeStart, STATUS_RANGE_DAYS);
		model.addAttribute("roomList", roomService.getRoomList(loginAdmin.getAdminIdx()));
		model.addAttribute("statusGrid", statusGrid);
		model.addAttribute("todayResvMap", roomStatusService.getTodayReservations(LocalDate.now()));
		model.addAttribute("rangeStart", rangeStart);
		model.addAttribute("rangeEnd", rangeStart.plusDays(STATUS_RANGE_DAYS - 1));
		return "Admin/room_status";
	}
	@GetMapping("admin_inquiry")
	public String adminInquiry(@RequestParam(value = "idx", required = false) Integer idx,
			@RequestParam(value = "status", required = false) String status,
			@RequestParam(defaultValue = "1") int page,
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
		List<InquiryDto> allList = inquiryService.getInquiryList(adminIdx, status);

		int maxlist = 10;
		int totcount = allList.size();
		int totpage = (totcount == 0) ? 1 : (totcount + maxlist - 1) / maxlist;
		int nowpage = Math.min(Math.max(page, 1), totpage);
		int offset = (nowpage - 1) * maxlist;
		List<InquiryDto> inquiryList = allList.subList(offset, Math.min(offset + maxlist, totcount));

		// 상태 필터는 페이지를 넘겨도 유지돼야 하므로 링크에 같이 싣는다
		String extraQuery = (status == null) ? ""
				: "&status=" + URLEncoder.encode(status, StandardCharsets.UTF_8);

		model.addAttribute("inquiryList", inquiryList);
		model.addAttribute("statusFilter", status);
		model.addAttribute("page", nowpage);
		model.addAttribute("totpage", totpage);
		model.addAttribute("totcount", totcount);
		model.addAttribute("pageSkip",
				PageIndex.pageList(nowpage, totpage, "/Admin/admin_inquiry", maxlist, extraQuery));
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
			@RequestParam(defaultValue = "1") int page,
			HttpSession session) {
		AdminDto loginAdmin = currentAdmin(session);
		if (loginAdmin == null) {
			return "redirect:/Admin/admin_login";
		}

		inquiryService.answerInquiry(inquiryIdx, loginAdmin.getAdminIdx(), inquiryAnswerContent);

		return "redirect:/Admin/admin_inquiry?idx=" + inquiryIdx + "&page=" + page;
	}

	/** 예약 현황 화면: 왼쪽 예약 목록 + 선택한(idx) 예약의 상세 패널을 함께 조회. idx가 없으면 첫 번째 예약. */
	@GetMapping("reservation_status")
	public String reservationStatus(@RequestParam(value = "idx", required = false) Integer idx,
			@RequestParam(defaultValue = "1") int page,
			HttpSession session, Model model) {
		AdminDto loginAdmin = currentAdmin(session);
		if (loginAdmin == null) {
			return "redirect:/Admin/admin_login";
		}

		List<AdminReservationListItemDto> allList =
				adminReservationService.getReservationList(loginAdmin.getAdminIdx());

		int maxlist = 10;
		int totcount = allList.size();
		int totpage = (totcount == 0) ? 1 : (totcount + maxlist - 1) / maxlist;
		int nowpage = Math.min(Math.max(page, 1), totpage);
		int offset = (nowpage - 1) * maxlist;
		List<AdminReservationListItemDto> reservationList =
				allList.subList(offset, Math.min(offset + maxlist, totcount));

		model.addAttribute("reservationList", reservationList);
		model.addAttribute("page", nowpage);
		model.addAttribute("totpage", totpage);
		model.addAttribute("totcount", totcount);
		model.addAttribute("pageSkip",
				PageIndex.pageList(nowpage, totpage, "/Admin/reservation_status", maxlist));

		if (!allList.isEmpty()) {
			// 내 예약 목록 안에서만 고른다 (다른 관리자의 예약번호를 idx로 넣어도 조회되지 않게)
			Integer selectedResvNum = allList.stream()
					.map(AdminReservationListItemDto::getResvNum)
					.filter(n -> n.equals(idx))
					.findFirst()
					.orElse(reservationList.get(0).getResvNum());
			AdminReservationDetailDto detail = adminReservationService.getReservationDetail(selectedResvNum);
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
			@RequestParam(value = "redirectTo", required = false, defaultValue = "admin_info_register") String redirectTo,
			HttpSession session) throws IOException {

		AdminDto loginAdmin = currentAdmin(session);
		if (loginAdmin == null) {
			return "redirect:/Admin/admin_login";
		}

		roomDto.setAdminIdx(loginAdmin.getAdminIdx());
		roomService.saveRoom(roomDto, roomImageFiles);

		return roomRedirect(redirectTo);
	}

	@PostMapping("room_delete")
	public String roomDelete(@RequestParam("roomIdx") Integer roomIdx,
			@RequestParam(value = "redirectTo", required = false, defaultValue = "admin_info_register") String redirectTo,
			HttpSession session) {
		AdminDto loginAdmin = currentAdmin(session);
		if (loginAdmin == null) {
			return "redirect:/Admin/admin_login";
		}
		roomService.deleteRoom(roomIdx, loginAdmin.getAdminIdx());
		return roomRedirect(redirectTo);
	}

	@PostMapping("room_toggle_sale")
	public String roomToggleSale(@RequestParam("roomIdx") Integer roomIdx,
			@RequestParam("roomSaleYn") String roomSaleYn,
			@RequestParam(value = "redirectTo", required = false, defaultValue = "room_status") String redirectTo,
			HttpSession session) {
		AdminDto loginAdmin = currentAdmin(session);
		if (loginAdmin == null) {
			return "redirect:/Admin/admin_login";
		}
		roomService.toggleSale(roomIdx, loginAdmin.getAdminIdx(), "Y".equals(roomSaleYn));
		return roomRedirect(redirectTo);
	}

	/** 당일 객실 관리 - 오늘 그 객실 예약을 체크인 표시(Y) / 취소(N) */
	@PostMapping("room_checkin")
	public String roomCheckin(@RequestParam("roomIdx") Integer roomIdx,
			@RequestParam("checkedIn") String checkedIn,
			HttpSession session) {
		AdminDto loginAdmin = currentAdmin(session);
		if (loginAdmin == null) {
			return "redirect:/Admin/admin_login";
		}
		roomStatusService.setCheckedIn(loginAdmin.getAdminIdx(), roomIdx, LocalDate.now(), "Y".equals(checkedIn));
		return "redirect:/Admin/room_status";
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
			@RequestParam(value = "redirectTo", required = false, defaultValue = "admin_info_register") String redirectTo,
			HttpSession session) throws IOException {

		AdminDto loginAdmin = currentAdmin(session);
		if (loginAdmin == null) {
			return "redirect:/Admin/admin_login";
		}

		planDto.setAdminIdx(loginAdmin.getAdminIdx());
		planService.savePlan(planDto, planImageFiles);

		return planRedirect(redirectTo);
	}

	@PostMapping("plan_delete")
	public String planDelete(@RequestParam("planIdx") Integer planIdx,
			@RequestParam(value = "redirectTo", required = false, defaultValue = "admin_info_register") String redirectTo,
			HttpSession session) {
		AdminDto loginAdmin = currentAdmin(session);
		if (loginAdmin == null) {
			return "redirect:/Admin/admin_login";
		}
		planService.deletePlan(planIdx, loginAdmin.getAdminIdx());
		return planRedirect(redirectTo);
	}

	@PostMapping("plan_toggle_sale")
	public String planToggleSale(@RequestParam("planIdx") Integer planIdx,
			@RequestParam("planSaleYn") String planSaleYn,
			@RequestParam(value = "redirectTo", required = false, defaultValue = "plan_sales") String redirectTo,
			HttpSession session) {
		AdminDto loginAdmin = currentAdmin(session);
		if (loginAdmin == null) {
			return "redirect:/Admin/admin_login";
		}
		planService.toggleSale(planIdx, loginAdmin.getAdminIdx(), "Y".equals(planSaleYn));
		return planRedirect(redirectTo);
	}

	@PostMapping("onsen_toggle_sale")
	public String onsenToggleSale(@RequestParam("onsenIdx") Integer onsenIdx,
			@RequestParam("onsenSaleYn") String onsenSaleYn,
			HttpSession session) {
		AdminDto loginAdmin = currentAdmin(session);
		if (loginAdmin == null) {
			return "redirect:/Admin/admin_login";
		}
		onsenService.toggleSale(onsenIdx, loginAdmin.getAdminIdx(), "Y".equals(onsenSaleYn));
		return "redirect:/Admin/plan_sales";
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
