package com.mnu.ryokanmaker.controller;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.mnu.ryokanmaker.dto.AdminPlanDto;
import com.mnu.ryokanmaker.dto.SearchConditionDto;
import com.mnu.ryokanmaker.service.NoticeService;
import com.mnu.ryokanmaker.service.OnsenService;
import com.mnu.ryokanmaker.service.ReservationService;
import com.mnu.ryokanmaker.service.RestaurantCourseService;
import com.mnu.ryokanmaker.service.RoomService;

@Controller
public class ReservationController {

    private static final Logger log = LoggerFactory.getLogger(ReservationController.class);

    // 사이트가 단일 료칸(清流庵) 기준이라 메인 화면 캐러셀은 이 관리자 소유 데이터로 고정 조회
    private static final int MAIN_ADMIN_IDX = 1;

    @Autowired
    private NoticeService noticeService;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private RoomService roomService;

    @Autowired
    private OnsenService onsenService;

    @Autowired
    private RestaurantCourseService restaurantCourseService;

    @GetMapping("/")
    public String mainIndex(Model model) {
        List<com.mnu.ryokanmaker.dto.NoticeDto> notices;
        try {
            List<com.mnu.ryokanmaker.dto.NoticeDto> all = noticeService.list();
            notices = all.size() > 3 ? all.subList(0, 3) : all;
        } catch (Exception e) {
            // 공지사항 미리보기는 부가 기능이라, DB 연결 문제로 메인 화면 전체가 죽지 않도록 방어
            log.warn("공지사항 조회 실패 - 메인 화면은 빈 목록으로 표시합니다.", e);
            notices = Collections.emptyList();
        }
        model.addAttribute("notices", notices);

        // 객실/온천/식사 캐러셀 : 각 카테고리에 등록된 모든 항목의 이미지를 하나의 목록으로 합쳐서 전달
        model.addAttribute("roomImages", collectImages(safeList(() -> roomService.getRoomList(MAIN_ADMIN_IDX)),
                com.mnu.ryokanmaker.dto.RoomDto::getImageUrls));
        model.addAttribute("onsenImages", collectImages(safeList(() -> onsenService.getOnsenList(MAIN_ADMIN_IDX)),
                com.mnu.ryokanmaker.dto.OnsenDto::getImageUrls));
        model.addAttribute("courseImages", collectImages(safeList(() -> restaurantCourseService.getCourseList(MAIN_ADMIN_IDX)),
                com.mnu.ryokanmaker.dto.RestaurantCourseDto::getImageUrls));

        return "index";
    }

    private <T> List<T> safeList(java.util.function.Supplier<List<T>> supplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
            log.warn("메인 화면 캐러셀용 목록 조회 실패 - 빈 목록으로 표시합니다.", e);
            return Collections.emptyList();
        }
    }

    private <T> List<String> collectImages(List<T> items, java.util.function.Function<T, List<String>> imageUrlsGetter) {
        return items.stream()
                .flatMap(item -> imageUrlsGetter.apply(item).stream())
                .toList();
    }

    /** 플랜 선택 화면 : templates/reservation/planSelect.html */
    @GetMapping("/reservation/plan")
    public String planSelect(Model model) {
        model.addAttribute("plans", reservationService.getAllPlans());
        return "reservation/planSelect";
    }

    /** 숙박예약(객실 + 식사 + 온천 선택) 화면 : templates/reservation/reservation.html */
    @GetMapping("/reservation/reservation")
    public String reservation(@RequestParam(defaultValue = "1") String planCode,
                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
                           @RequestParam(defaultValue = "2") int adultCount,
                           @RequestParam(defaultValue = "0") int childCount,
                           @RequestParam(defaultValue = "1") int roomCount,
                           Model model) {

        if (checkIn == null) {
            checkIn = LocalDate.now().plusDays(1);
        }
        if (checkOut == null || !checkOut.isAfter(checkIn)) {
            checkOut = checkIn.plusDays(1);
        }

        List<AdminPlanDto> plans = reservationService.getAllPlans();
        AdminPlanDto selectedPlan = plans.stream()
                .filter(p -> String.valueOf(p.getPlanIdx()).equals(planCode))
                .findFirst()
                .orElseGet(() -> plans.get(0));

        model.addAttribute("selectedPlan", selectedPlan);
        model.addAttribute("searchCondition", new SearchConditionDto(checkIn, checkOut, adultCount, childCount, roomCount));
        model.addAttribute("rooms", reservationService.getRoomAvailability(checkIn, checkOut, adultCount, childCount));
        model.addAttribute("courses", reservationService.getCourses());
        model.addAttribute("baths", reservationService.getBathAvailability());

        return "reservation/reservation";
    }
}
