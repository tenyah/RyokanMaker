package com.mnu.ryokanmaker.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mnu.ryokanmaker.domain.CourseDTO;
import com.mnu.ryokanmaker.domain.OnsenDTO;
import com.mnu.ryokanmaker.domain.PlanDTO;
import com.mnu.ryokanmaker.domain.RoomDTO;
import com.mnu.ryokanmaker.domain.RoomReservationDTO;
import com.mnu.ryokanmaker.dto.BathAvailabilityDto;
import com.mnu.ryokanmaker.dto.RoomAvailabilityDto;
import com.mnu.ryokanmaker.dto.SlotDto;
import com.mnu.ryokanmaker.mappers.CourseMapper;
import com.mnu.ryokanmaker.mappers.OnsenMapper;
import com.mnu.ryokanmaker.mappers.PlanMapper;
import com.mnu.ryokanmaker.mappers.RoomMapper;
import com.mnu.ryokanmaker.mappers.RoomReservationMapper;

@Service
public class ReservationService {

    @Autowired
    private PlanMapper planMapper;

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private RoomMapper roomMapper;

    @Autowired
    private RoomReservationMapper roomReservationMapper;

    @Autowired
    private OnsenMapper onsenMapper;

    public List<PlanDTO> getAllPlans() {
        return planMapper.findAll();
    }

    public List<CourseDTO> getCourses() {
        return courseMapper.findAll();
    }

    public List<RoomAvailabilityDto> getRoomAvailability(LocalDate checkIn, LocalDate checkOut,
                                                          int adultCount, int childCount) {
        long requestedPeople = adultCount + childCount;
        List<RoomReservationDTO> reserved = roomReservationMapper.findOverlapping(checkIn, checkOut);

        Long basePrice = roomMapper.findMinPrice(); // 현재 판매중인 방 중 최저가 — 플랜 가격에 이미 포함된 금액
        if (basePrice == null) basePrice = 0L;

        List<RoomAvailabilityDto> rooms = new ArrayList<>();
        for (RoomDTO room : roomMapper.findAll()) {
            if (room.getRoomPeople() < requestedPeople) {
                continue;
            }
            boolean booked = reserved.stream()
                    .anyMatch(r -> r.getRoomIdx().equals(room.getRoomIdx())
                            && r.getResvCheckIn().isBefore(checkOut)
                            && r.getResvCheckOut().isAfter(checkIn));
            long extraCharge = Math.max(0, room.getRoomPrice() - basePrice);
            rooms.add(new RoomAvailabilityDto(room.getRoomIdx(), room.getRoomName(),
                    room.getRoomPeople(), room.getRoomPrice(), extraCharge, !booked));
        }
        return rooms;
    }

    // TODO: 시간대별 예약 가능 여부는 나중에 OnsenReservationMapper로 교체 (현재는 목업)
    public List<BathAvailabilityDto> getBathAvailability() {
        List<String> times = List.of("15:00", "16:00", "17:00", "18:00", "19:00", "20:00", "21:00");

        List<OnsenDTO> onsenList = onsenMapper.findAll();
        List<BathAvailabilityDto> baths = new ArrayList<>();
        for (int i = 0; i < onsenList.size(); i++) {
            OnsenDTO onsen = onsenList.get(i);
            List<SlotDto> slots = new ArrayList<>();
            for (int t = 0; t < times.size(); t++) {
                boolean available = (i + t) % 3 != 0;
                slots.add(new SlotDto(times.get(t), available));
            }
            baths.add(new BathAvailabilityDto(onsen.getOnsenIdx(), onsen.getOnsenName(), slots));
        }
        return baths;
    }
 // 최종 결제금액 = 플랜가격 + (선택한 방 가격 - 현재 최저가 방 기준가)
    public Long calculateFinalPrice(Long planIdx, Long roomIdx) {
        PlanDTO plan = planMapper.findById(planIdx);
        RoomDTO room = roomMapper.findById(roomIdx);

        Long basePrice = roomMapper.findMinPrice();
        if (basePrice == null) basePrice = 0L;

        long extra = room.getRoomPrice() - basePrice;
        if (extra < 0) extra = 0L; // 혹시나 역전됐을 때 방어코드

        return plan.getPlanPrice() + extra;
    }
    
}