package com.mnu.ryokanmaker.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.mnu.ryokanmaker.domain.PlanDTO;
import com.mnu.ryokanmaker.dto.SearchConditionDto;
import com.mnu.ryokanmaker.service.ReservationService;

@Controller
public class ReservationController {

    @Autowired
    private ReservationService reservationService;

    @GetMapping("/")
    public String mainIndex() {
        return "index";
    }

    @GetMapping("/reservation/plan")
    public String planSelect(Model model) {
        model.addAttribute("plans", reservationService.getAllPlans());
        return "reservation/planSelect";
    }

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

        List<PlanDTO> plans = reservationService.getAllPlans();
        PlanDTO selectedPlan = plans.stream()
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