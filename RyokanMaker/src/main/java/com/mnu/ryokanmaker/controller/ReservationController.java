package com.mnu.ryokanmaker.controller;

import com.mnu.ryokanmaker.dto.BathAvailabilityDto;
import com.mnu.ryokanmaker.dto.CourseDto;
import com.mnu.ryokanmaker.dto.DayStatusDto;
import com.mnu.ryokanmaker.dto.PlanDto;
import com.mnu.ryokanmaker.dto.RoomAvailabilityDto;
import com.mnu.ryokanmaker.dto.SearchConditionDto;
import com.mnu.ryokanmaker.dto.SlotDto;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 화면 확인용 컨트롤러. 지금은 DB 연동 전이라 화면과 똑같은 모양의 더미(샘플) 데이터를
 * 그대로 모델에 담아 내려줍니다. 나중에 서비스/DAO가 준비되면 아래 더미 데이터를
 * 실제 조회 결과로 바꿔치기만 하면 됩니다.
 */
@Controller
public class ReservationController {

    @GetMapping("/")
    public String mainIndex() {
        return "index";
    }

    /** 플랜 선택 화면 : templates/reservation/planSelect.html */
    @GetMapping("/reservation/plan")
    public String planSelect(Model model) {
        model.addAttribute("plans", buildPlans());
        return "reservation/planSelect";
    }

    /** 숙박예약(캘린더 + 식사 + 온천) 화면 : templates/reservation/booking.html */
    @GetMapping("/reservation/booking")
    public String booking(@RequestParam(defaultValue = "ROOM_MEAL") String planCode, Model model) {

        // planCode에 해당하는 플랜을 찾고, 없으면 기본값(객실+식사)으로
        PlanDto selectedPlan = buildPlans().stream()
                .filter(p -> p.getCode().equals(planCode))
                .findFirst()
                .orElseGet(() -> buildPlans().get(1));

        LocalDate checkIn = LocalDate.of(2026, 9, 13);
        LocalDate checkOut = checkIn.plusDays(1);

        // ↓↓↓ booking.html이 참조하는 모델 값들. 이게 하나라도 빠지면 화면에서 에러 납니다.
        model.addAttribute("selectedPlan", selectedPlan);
        model.addAttribute("searchCondition", new SearchConditionDto(checkIn, checkOut, 2, 0, 1));
        model.addAttribute("calendarDates", buildCalendarDates(checkIn));
        model.addAttribute("rooms", buildRooms(checkIn));
        model.addAttribute("courses", buildCourses());
        model.addAttribute("baths", buildBaths());

        return "reservation/booking";
    }

    // ---------------------------------------------------------------
    // 아래는 전부 화면 확인용 더미 데이터입니다. (실제로는 Service에서 조회)
    // ---------------------------------------------------------------

    private List<PlanDto> buildPlans() {
        List<PlanDto> plans = new ArrayList<>();
        plans.add(new PlanDto("ROOM_ONLY", "객실 플랜", "/images/room-sakura.jpg",
                true, false, false,
                "숙박만 필요하신 분을 위한 가장 기본적인 구성입니다. 식사와 온천은 현지에서 별도로 이용하실 수 있습니다.",
                780000));
        plans.add(new PlanDto("ROOM_MEAL", "객실 + 식사 플랜", "/images/course-moon.jpg",
                true, true, false,
                "『계절의 맛 가이세키』 등 코스 요리가 포함된 구성입니다. 여행의 즐거움을 미식으로 더하고 싶은 분께 추천드립니다.",
                848508));
        plans.add(new PlanDto("ROOM_MEAL_ONSEN", "객실 + 식사 + 온천 플랜", "/images/onsen-bath.jpg",
                true, true, true,
                "숙박, 식사, 전세탕 온천까지 모두 포함된 가장 여유로운 구성입니다.",
                1020000));
        plans.add(new PlanDto("ROOM_ONSEN", "객실 + 온천 플랜", "/images/course-snow.jpg",
                true, false, true,
                "전세탕 온천 이용이 포함된 구성입니다. 식사는 현지에서 자유롭게 즐기실 수 있습니다.",
                867321));
        return plans;
    }

    private List<LocalDate> buildCalendarDates(LocalDate start) {
        List<LocalDate> dates = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            dates.add(start.plusDays(i));
        }
        return dates;
    }

    private List<RoomAvailabilityDto> buildRooms(LocalDate start) {
        List<RoomAvailabilityDto> rooms = new ArrayList<>();

        rooms.add(new RoomAvailabilityDto("사쿠라", List.of(
                new DayStatusDto(start, DayStatusDto.OK, 848508),
                new DayStatusDto(start.plusDays(1), DayStatusDto.ASK, 0),
                new DayStatusDto(start.plusDays(2), DayStatusDto.OK, 848508),
                new DayStatusDto(start.plusDays(3), DayStatusDto.NO, 0),
                new DayStatusDto(start.plusDays(4), DayStatusDto.OK, 867321),
                new DayStatusDto(start.plusDays(5), DayStatusDto.OK, 867321),
                new DayStatusDto(start.plusDays(6), DayStatusDto.ASK, 0)
        )));

        rooms.add(new RoomAvailabilityDto("[객실명 2]", List.of(
                new DayStatusDto(start, DayStatusDto.NO, 0),
                new DayStatusDto(start.plusDays(1), DayStatusDto.OK, 780000),
                new DayStatusDto(start.plusDays(2), DayStatusDto.OK, 780000),
                new DayStatusDto(start.plusDays(3), DayStatusDto.OK, 780000),
                new DayStatusDto(start.plusDays(4), DayStatusDto.NO, 0),
                new DayStatusDto(start.plusDays(5), DayStatusDto.ASK, 0),
                new DayStatusDto(start.plusDays(6), DayStatusDto.OK, 812000)
        )));

        rooms.add(new RoomAvailabilityDto("[객실명 3]", List.of(
                new DayStatusDto(start, DayStatusDto.OK, 1020000),
                new DayStatusDto(start.plusDays(1), DayStatusDto.OK, 1020000),
                new DayStatusDto(start.plusDays(2), DayStatusDto.ASK, 0),
                new DayStatusDto(start.plusDays(3), DayStatusDto.OK, 1020000),
                new DayStatusDto(start.plusDays(4), DayStatusDto.OK, 1020000),
                new DayStatusDto(start.plusDays(5), DayStatusDto.NO, 0),
                new DayStatusDto(start.plusDays(6), DayStatusDto.NO, 0)
        )));

        return rooms;
    }

    private List<CourseDto> buildCourses() {
        List<CourseDto> courses = new ArrayList<>();
        courses.add(new CourseDto(
                "달 코스 『계절의 맛 가이세키』",
                "/images/course-moon.jpg",
                "홋카이도의 맛을 즐기는 계절 가이세키 (전 8품). 스탠다드 코스입니다.",
                true));
        courses.add(new CourseDto(
                "눈 코스 (프리미엄)",
                "/images/course-snow.jpg",
                null,
                false));
        return courses;
    }

    private List<BathAvailabilityDto> buildBaths() {
        List<String> times = List.of("15:00", "16:00", "17:00", "18:00", "19:00", "20:00", "21:00");

        List<SlotDto> outdoorSlots = List.of(
                new SlotDto(times.get(0), true), new SlotDto(times.get(1), false),
                new SlotDto(times.get(2), true), new SlotDto(times.get(3), true),
                new SlotDto(times.get(4), false), new SlotDto(times.get(5), true),
                new SlotDto(times.get(6), true)
        );
        List<SlotDto> indoorSlots = List.of(
                new SlotDto(times.get(0), false), new SlotDto(times.get(1), true),
                new SlotDto(times.get(2), true), new SlotDto(times.get(3), false),
                new SlotDto(times.get(4), true), new SlotDto(times.get(5), true),
                new SlotDto(times.get(6), false)
        );

        List<BathAvailabilityDto> baths = new ArrayList<>();
        baths.add(new BathAvailabilityDto("노천탕 (露天湯)", outdoorSlots));
        baths.add(new BathAvailabilityDto("실내탕 (内湯)", indoorSlots));
        return baths;
    }
}
