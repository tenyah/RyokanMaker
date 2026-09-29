package com.mnu.ryokanmaker.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.mnu.ryokanmaker.config.TenantInterceptor;
import com.mnu.ryokanmaker.domain.AdminDto;
import com.mnu.ryokanmaker.service.FacilityService;
import com.mnu.ryokanmaker.service.OnsenService;
import com.mnu.ryokanmaker.service.RestaurantCourseService;
import com.mnu.ryokanmaker.service.RoomService;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 헤더 상단 메뉴(객실 / 온천 / 식사 / 시설) 소개 화면. /r/{adminId} 밑이라 어느 료칸인지는
 * TenantInterceptor가 미리 찾아둔다(존재하지 않는 adminId면 이 컨트롤러까지 오지 않고 404).
 * 예약 여부와 무관하게 시설 소개를 보여주는 화면이라, 판매중(SALE_YN='Y') 항목만 노출한다.
 * (시설(FACILITY)은 노출여부 컬럼이 없어 등록된 항목을 전부 보여준다.)
 */
@Controller
@RequestMapping("/r/{adminId}")
public class SiteController {

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
    public String rooms(@PathVariable String adminId, HttpServletRequest request, Model model) {
        AdminDto tenant = TenantInterceptor.currentTenant(request);
        List<com.mnu.ryokanmaker.domain.RoomDto> rooms = roomService.getRoomList(tenant.getAdminIdx()).stream()
                .filter(room -> "Y".equals(room.getRoomSaleYn()))
                .toList();
        model.addAttribute("rooms", rooms);
        return "rooms";
    }

    /** 온천 소개 : templates/onsen.html */
    @GetMapping("/onsen")
    public String onsen(@PathVariable String adminId, HttpServletRequest request, Model model) {
        AdminDto tenant = TenantInterceptor.currentTenant(request);
        List<com.mnu.ryokanmaker.domain.OnsenDto> onsens = onsenService.getOnsenList(tenant.getAdminIdx()).stream()
                .filter(onsen -> "Y".equals(onsen.getOnsenSaleYn()))
                .toList();
        model.addAttribute("onsens", onsens);
        return "onsen";
    }

    /** 식사 소개 : templates/dining.html */
    @GetMapping("/dining")
    public String dining(@PathVariable String adminId, HttpServletRequest request, Model model) {
        AdminDto tenant = TenantInterceptor.currentTenant(request);
        List<com.mnu.ryokanmaker.domain.RestaurantCourseDto> courses = restaurantCourseService.getCourseList(tenant.getAdminIdx()).stream()
                .filter(course -> "Y".equals(course.getRestaurantSaleYn()))
                .toList();
        model.addAttribute("courses", courses);
        return "dining";
    }

    /** 시설 소개 : templates/facility.html */
    @GetMapping("/facility")
    public String facility(@PathVariable String adminId, HttpServletRequest request, Model model) {
        AdminDto tenant = TenantInterceptor.currentTenant(request);
        model.addAttribute("facilities", facilityService.getFacilityList(tenant.getAdminIdx()));
        return "facility";
    }
}
