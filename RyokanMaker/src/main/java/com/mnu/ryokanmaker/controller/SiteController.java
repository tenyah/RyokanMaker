package com.mnu.ryokanmaker.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.mnu.ryokanmaker.service.FacilityService;
import com.mnu.ryokanmaker.service.OnsenService;
import com.mnu.ryokanmaker.service.RestaurantCourseService;
import com.mnu.ryokanmaker.service.RoomService;

/**
 * 헤더 상단 메뉴(객실 / 온천 / 식사 / 시설) 소개 화면.
 * 예약 여부와 무관하게 시설 소개를 보여주는 화면이라, 판매중(SALE_YN='Y') 항목만 노출한다.
 * (시설(FACILITY)은 노출여부 컬럼이 없어 등록된 항목을 전부 보여준다.)
 */
@Controller
public class SiteController {

    // 사이트가 단일 료칸(清流庵) 기준이라 소개 화면도 이 관리자 소유 데이터로 고정 조회
    private static final int MAIN_ADMIN_IDX = 1;

    @Autowired
    private RoomService roomService;

    @Autowired
    private OnsenService onsenService;

    @Autowired
    private RestaurantCourseService restaurantCourseService;

    @Autowired
    private FacilityService facilityService;

    /** 객실 소개 : templates/rooms.html */
    @GetMapping("/rooms")
    public String rooms(Model model) {
        List<com.mnu.ryokanmaker.domain.RoomDto> rooms = roomService.getRoomList(MAIN_ADMIN_IDX).stream()
                .filter(room -> "Y".equals(room.getRoomSaleYn()))
                .toList();
        model.addAttribute("rooms", rooms);
        return "rooms";
    }

    /** 온천 소개 : templates/onsen.html */
    @GetMapping("/onsen")
    public String onsen(Model model) {
        List<com.mnu.ryokanmaker.domain.OnsenDto> onsens = onsenService.getOnsenList(MAIN_ADMIN_IDX).stream()
                .filter(onsen -> "Y".equals(onsen.getOnsenSaleYn()))
                .toList();
        model.addAttribute("onsens", onsens);
        return "onsen";
    }

    /** 식사 소개 : templates/dining.html */
    @GetMapping("/dining")
    public String dining(Model model) {
        List<com.mnu.ryokanmaker.domain.RestaurantCourseDto> courses = restaurantCourseService.getCourseList(MAIN_ADMIN_IDX).stream()
                .filter(course -> "Y".equals(course.getRestaurantSaleYn()))
                .toList();
        model.addAttribute("courses", courses);
        return "dining";
    }

    /** 시설 소개 : templates/facility.html */
    @GetMapping("/facility")
    public String facility(Model model) {
        model.addAttribute("facilities", facilityService.getFacilityList(MAIN_ADMIN_IDX));
        return "facility";
    }
}
