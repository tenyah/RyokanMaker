package com.mnu.ryokanmaker.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mnu.ryokanmaker.dto.AdminPlanDto;
import com.mnu.ryokanmaker.dto.BathAvailabilityDto;
import com.mnu.ryokanmaker.dto.OnsenDto;
import com.mnu.ryokanmaker.dto.RestaurantCourseDto;
import com.mnu.ryokanmaker.dto.RoomAvailabilityDto;
import com.mnu.ryokanmaker.dto.RoomDto;
import com.mnu.ryokanmaker.dto.RoomReservationDto;
import com.mnu.ryokanmaker.dto.SlotDto;
import com.mnu.ryokanmaker.mapper.OnsenMapper;
import com.mnu.ryokanmaker.mapper.PlanMapper;
import com.mnu.ryokanmaker.mapper.RestaurantCourseMapper;
import com.mnu.ryokanmaker.mapper.RoomMapper;
import com.mnu.ryokanmaker.mapper.RoomReservationMapper;

@Service
public class ReservationService {

    @Autowired
    private PlanMapper planMapper;

    @Autowired
    private RestaurantCourseMapper restaurantCourseMapper;

    @Autowired
    private RoomMapper roomMapper;

    @Autowired
    private RoomReservationMapper roomReservationMapper;

    @Autowired
    private OnsenMapper onsenMapper;

    public List<AdminPlanDto> getAllPlans() {
        return planMapper.findAllOnSale();
    }

    public List<RestaurantCourseDto> getCourses() {
        return restaurantCourseMapper.findAllOnSale();
    }

    public List<RoomAvailabilityDto> getRoomAvailability(LocalDate checkIn, LocalDate checkOut,
                                                          int adultCount, int childCount) {
        int requestedPeople = adultCount + childCount;
        List<RoomReservationDto> reserved = roomReservationMapper.findOverlapping(checkIn, checkOut);

        Long basePrice = roomMapper.findMinPriceByLevel("3"); // 작은방 기준 — 플랜 가격에 이미 포함된 금액
        if (basePrice == null) basePrice = 0L;

        List<RoomAvailabilityDto> rooms = new ArrayList<>();
        for (RoomDto room : roomMapper.findAllOnSale()) {
            if (room.getRoomPeople() < requestedPeople) {
                continue;
            }
            boolean booked = reserved.stream()
                    .anyMatch(r -> r.getRoomIdx().equals(room.getRoomIdx())
                            && r.getResvCheckIn().isBefore(checkOut)
                            && r.getResvCheckOut().isAfter(checkIn));
            int extraCharge = (int) Math.max(0, room.getRoomPrice() - basePrice);
            rooms.add(new RoomAvailabilityDto(room.getRoomIdx(), room.getRoomName(),
                    room.getRoomPeople(), room.getRoomPrice(), extraCharge, !booked));
        }
        return rooms;
    }

    // TODO: 시간대별 예약 가능 여부는 나중에 OnsenReservationMapper로 교체 (현재는 목업)
    public List<BathAvailabilityDto> getBathAvailability() {
        List<String> times = List.of("15:00", "16:00", "17:00", "18:00", "19:00", "20:00", "21:00");

        List<OnsenDto> onsenList = onsenMapper.findAllOnSale();
        List<BathAvailabilityDto> baths = new ArrayList<>();
        for (int i = 0; i < onsenList.size(); i++) {
            OnsenDto onsen = onsenList.get(i);
            List<SlotDto> slots = new ArrayList<>();
            for (int t = 0; t < times.size(); t++) {
                boolean available = (i + t) % 3 != 0;
                slots.add(new SlotDto(times.get(t), available));
            }
            baths.add(new BathAvailabilityDto(onsen.getOnsenIdx(), onsen.getOnsenName(), slots));
        }
        return baths;
    }

    /** 최종 결제금액 = 플랜가격 + (선택한 방 가격 - 작은방 기준가) */
    public Integer calculateFinalPrice(Integer planIdx, Integer roomIdx) {
        AdminPlanDto plan = planMapper.findById(planIdx);
        RoomDto room = roomMapper.findById(roomIdx);

        Long basePrice = roomMapper.findMinPriceByLevel("3"); // 작은방 기준
        if (basePrice == null) basePrice = 0L;

        long extra = room.getRoomPrice() - basePrice;
        if (extra < 0) extra = 0L; // 혹시나 역전됐을 때 방어코드

        return (int) (plan.getPlanPrice() + extra);
    }
}
