package com.mnu.ryokanmaker.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mnu.ryokanmaker.domain.RoomDayStatusDto;
import com.mnu.ryokanmaker.domain.RoomDto;
import com.mnu.ryokanmaker.domain.RoomReservationDto;
import com.mnu.ryokanmaker.domain.RoomStatusRowDto;
import com.mnu.ryokanmaker.mapper.RoomMapper;
import com.mnu.ryokanmaker.mapper.RoomReservationMapper;

/** 관리자 - 객실 현황(room_status.html) 캘린더 조회 서비스. */
@Service
public class RoomStatusService {

    /** 체크인 표시된 예약의 상태값. 취소하면 STATUS_RESERVED로 되돌린다. */
    public static final String STATUS_CHECKED_IN = "체크인";
    public static final String STATUS_RESERVED = PaymentReservationService.STATUS_RESERVED;

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
                RoomReservationDto hit = reserved.stream().filter(r ->
                        r.getRoomIdx().equals(room.getRoomIdx())
                                && !day.isBefore(r.getResvCheckIn())
                                && day.isBefore(r.getResvCheckOut()))
                        .findFirst().orElse(null);
                dayStatuses.add(new RoomDayStatusDto(day, hit != null,
                        hit != null && STATUS_CHECKED_IN.equals(hit.getResvStatus())));
            }
            grid.add(new RoomStatusRowDto(room, dayStatuses));
        }
        return grid;
    }

    /** 오늘 각 객실에 걸려 있는 예약 (key: roomIdx). 당일 객실 관리 화면의 체크인 상태 표시용. */
    public Map<Integer, RoomReservationDto> getTodayReservations(LocalDate today) {
        Map<Integer, RoomReservationDto> map = new HashMap<>();
        for (RoomReservationDto r : roomReservationMapper.findOverlapping(today, today.plusDays(1))) {
            map.putIfAbsent(r.getRoomIdx(), r);
        }
        return map;
    }

    /** 오늘 그 객실 예약을 체크인 표시(checkedIn=true) 또는 취소(false). 예약완료<->체크인 사이만 전환한다. */
    @Transactional
    public void setCheckedIn(Integer adminIdx, Integer roomIdx, LocalDate today, boolean checkedIn) {
        String from = checkedIn ? STATUS_RESERVED : STATUS_CHECKED_IN;
        String to = checkedIn ? STATUS_CHECKED_IN : STATUS_RESERVED;
        roomReservationMapper.updateResvStatusByRoomAndDate(adminIdx, roomIdx, today, from, to);
        roomReservationMapper.updateRoomResvStatusByRoomAndDate(adminIdx, roomIdx, today, from, to);
    }
}
