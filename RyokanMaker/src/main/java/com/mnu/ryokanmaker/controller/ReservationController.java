package com.mnu.ryokanmaker.controller;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.mnu.ryokanmaker.dto.AdminPlanDto;
import com.mnu.ryokanmaker.dto.SearchConditionDto;
import com.mnu.ryokanmaker.service.GeminiTranslationService;
import com.mnu.ryokanmaker.service.NoticeService;
import com.mnu.ryokanmaker.service.ReservationService;

import jakarta.servlet.http.HttpSession;

@Controller
public class ReservationController {

    private static final Logger log = LoggerFactory.getLogger(ReservationController.class);

    @Autowired
    private NoticeService noticeService;

    @Autowired
    private GeminiTranslationService translationService;

    @Autowired
    private ReservationService reservationService;

    @GetMapping("/")
    public String mainIndex(Model model) {
        List<com.mnu.ryokanmaker.dto.NoticeDto> notices;
        try {
            List<com.mnu.ryokanmaker.dto.NoticeDto> all = noticeService.list();
            notices = all.size() > 3 ? all.subList(0, 3) : all;
            for (com.mnu.ryokanmaker.dto.NoticeDto notice : notices) {
                notice.setNoticeTitle(translationService.translate(notice.getNoticeTitle(), LocaleContextHolder.getLocale()));
            }
        } catch (Exception e) {
            // 공지사항 미리보기는 부가 기능이라, DB 연결 문제로 메인 화면 전체가 죽지 않도록 방어
            log.warn("공지사항 조회 실패 - 메인 화면은 빈 목록으로 표시합니다.", e);
            notices = Collections.emptyList();
        }
        model.addAttribute("notices", notices);
        return "index";
    }

    /** 플랜 선택 화면 : templates/reservation/planSelect.html (비회원도 열람 가능) */
    @GetMapping("/reservation/plan")
    public String planSelect(Model model) {
        model.addAttribute("plans", reservationService.getAllPlans());
        return "reservation/planSelect";
    }

    /**
     * 숙박예약(객실 + 식사 + 온천 선택) 화면 : templates/reservation/reservation.html
     * 플랜 선택까지는 비회원도 볼 수 있지만, 실제 예약 단계인 이 화면부터는 로그인이 필요하다
     * (RESERVATION.USER_MAIL이 MEMBER를 FK로 참조하므로 회원만 예약 가능).
     */
    @GetMapping("/reservation/reservation")
    public String reservation(@RequestParam(defaultValue = "1") String planCode,
                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
                           @RequestParam(defaultValue = "2") int adultCount,
                           @RequestParam(defaultValue = "0") int childCount,
                           @RequestParam(defaultValue = "1") int roomCount,
                           Model model, HttpSession session) {

        if (session.getAttribute("loginMember") == null) {
            return "redirect:/member/login";
        }

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
