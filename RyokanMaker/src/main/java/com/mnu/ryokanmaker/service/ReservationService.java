package com.mnu.ryokanmaker.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mnu.ryokanmaker.domain.CourseDTO;
import com.mnu.ryokanmaker.domain.PlanDTO;
import com.mnu.ryokanmaker.domain.RoomDTO;
import com.mnu.ryokanmaker.domain.RoomReservationDTO;
import com.mnu.ryokanmaker.dto.BathAvailabilityDto;
import com.mnu.ryokanmaker.dto.DayStatusDto;
import com.mnu.ryokanmaker.dto.RoomAvailabilityDto;
import com.mnu.ryokanmaker.dto.SlotDto;
import com.mnu.ryokanmaker.mappers.CourseMapper;
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

    public List<PlanDTO> getAllPlans() {
        return planMapper.findAll();
    }

    public List<CourseDTO> getCourses() {
        return courseMapper.findAll();
    }

    public List<RoomAvailabilityDto> getRoomAvailability(List<LocalDate> calendarDates) {
        List<RoomDTO> roomList = roomMapper.findAll();

        LocalDate rangeStart = calendarDates.get(0);
        LocalDate rangeEnd = calendarDates.get(calendarDates.size() - 1).plusDays(1);
        List<RoomReservationDTO> reserved = roomReservationMapper.findOverlapping(rangeStart, rangeEnd);

        List<RoomAvailabilityDto> rooms = new ArrayList<>();
        for (RoomDTO room : roomList) {
            List<DayStatusDto> days = new ArrayList<>();
            for (LocalDate date : calendarDates) {
                boolean booked = reserved.stream()
                        .anyMatch(r -> r.getRoomIdx().equals(room.getRoomIdx())
                                && !date.isBefore(r.getResvCheckIn())
                                && date.isBefore(r.getResvCheckOut()));

                String status = booked ? DayStatusDto.NO : DayStatusDto.OK;
                int price = booked ? 0 : room.getRoomPrice().intValue();
                days.add(new DayStatusDto(date, status, price));
            }
            rooms.add(new RoomAvailabilityDto(room.getRoomName(), days));
        }
        return rooms;
    }

    // TODO: 나중에 OnsenReservationMapper로 교체
    public List<BathAvailabilityDto> getBathAvailability() {
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