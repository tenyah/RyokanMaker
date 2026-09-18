package com.mnu.ryokanmaker.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mnu.ryokanmaker.domain.RoomDayStatusDto;
import com.mnu.ryokanmaker.domain.RoomDto;
import com.mnu.ryokanmaker.domain.RoomReservationDto;
import com.mnu.ryokanmaker.domain.RoomStatusRowDto;
import com.mnu.ryokanmaker.mapper.RoomMapper;
import com.mnu.ryokanmaker.mapper.RoomReservationMapper;

/** 관리자 - 객실 현황(room_status.html) 캘린더 조회 서비스. */
@Service
public class RoomStatusService {

    @Autowired
    private RoomMapper roomMapper;

    @Autowired
    private RoomReservationMapper roomReservationMapper;

    /** rangeStart부터 days일간, 로그인한 관리자의 객실별 날짜별 예약 여부를 계산한다. */
    public List<RoomStatusRowDto> getStatusGrid(Integer adminIdx, LocalDate rangeStart, int days) {
        LocalDate rangeEnd = rangeStart.plusDays(days);

        List<RoomDto> rooms = roomMapper.selectRoomsByAdmin(adminIdx);
        List<RoomReservationDto> reserved = roomReservationMapper.findOverlapping(rangeStart, rangeEnd);

        List<RoomStatusRowDto> grid = new ArrayList<>();
        for (RoomDto room : rooms) {
            List<RoomDayStatusDto> dayStatuses = new ArrayList<>();
            for (int i = 0; i < days; i++) {
                LocalDate day = rangeStart.plusDays(i);
                boolean booked = reserved.stream().anyMatch(r ->
                        r.getRoomIdx().equals(room.getRoomIdx())
                                && !day.isBefore(r.getResvCheckIn())
                                && day.isBefore(r.getResvCheckOut()));
                dayStatuses.add(new RoomDayStatusDto(day, booked));
            }
            grid.add(new RoomStatusRowDto(room, dayStatuses));
        }
        return grid;
    }
}
