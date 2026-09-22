package com.mnu.ryokanmaker.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mnu.ryokanmaker.domain.CheckInOutRowDto;
import com.mnu.ryokanmaker.domain.DashboardStatsDto;
import com.mnu.ryokanmaker.domain.RoomDto;
import com.mnu.ryokanmaker.domain.RoomReservationDto;
import com.mnu.ryokanmaker.mapper.RevenueMapper;
import com.mnu.ryokanmaker.mapper.RoomMapper;
import com.mnu.ryokanmaker.mapper.RoomReservationMapper;

/** 관리자 - 대시보드(dashboard.html)의 오늘 운영 요약 조회 서비스. */
@Service
public class DashboardService {

    @Autowired
    private RoomMapper roomMapper;

    @Autowired
    private RoomReservationMapper roomReservationMapper;

    @Autowired
    private RevenueMapper revenueMapper;

    /** 최상단 KPI 카드 7종 (재실 현황, 오늘 체크인/아웃 건수, 오늘 매출, 당월 누적 매출). */
    public DashboardStatsDto getStats(Integer adminIdx) {
        LocalDate today = LocalDate.now();

        List<RoomDto> rooms = roomMapper.selectRoomsByAdmin(adminIdx);
        int roomsTotal = rooms.size();
        Set<Integer> adminRoomIdx = rooms.stream().map(RoomDto::getRoomIdx).collect(Collectors.toSet());

        List<RoomReservationDto> overlappingToday = roomReservationMapper.findOverlapping(today, today.plusDays(1));
        long occupiedToday = overlappingToday.stream()
                .filter(r -> adminRoomIdx.contains(r.getRoomIdx()))
                .map(RoomReservationDto::getRoomIdx)
                .distinct()
                .count();
        int occRate = roomsTotal == 0 ? 0 : (int) Math.round(occupiedToday * 100.0 / roomsTotal);

        List<CheckInOutRowDto> checkIns = revenueMapper.findCheckInsByAdminAndDate(adminIdx, today);
        checkIns.forEach(this::markCompleted);
        int checkInsToday = checkIns.size();
        int checkInsCompleted = (int) checkIns.stream().filter(CheckInOutRowDto::isCompleted).count();
        int checkOutsToday = revenueMapper.findCheckOutsByAdminAndDate(adminIdx, today).size();

        int todayRevenue = paidTotal(roomReservationMapper.findByAdminAndCheckInRange(adminIdx, today, today.plusDays(1)));

        YearMonth ym = YearMonth.now();
        int monthToDateRevenue = paidTotal(
                roomReservationMapper.findByAdminAndCheckInRange(adminIdx, ym.atDay(1), today.plusDays(1)));

        return new DashboardStatsDto(roomsTotal, (int) occupiedToday, occRate,
                checkInsToday, checkInsCompleted, checkOutsToday, todayRevenue, monthToDateRevenue);
    }

    private int paidTotal(List<RoomReservationDto> rows) {
        return rows.stream()
                .filter(r -> RevenueService.PAID.equals(r.getResvPayStatus()))
                .mapToInt(RoomReservationDto::getResvPrice)
                .sum();
    }

    public List<CheckInOutRowDto> getTodayCheckIns(Integer adminIdx) {
        List<CheckInOutRowDto> rows = revenueMapper.findCheckInsByAdminAndDate(adminIdx, LocalDate.now());
        rows.forEach(this::markRequest);
        rows.forEach(this::markCompleted);
        return rows;
    }

    public List<CheckInOutRowDto> getTodayCheckOuts(Integer adminIdx) {
        List<CheckInOutRowDto> rows = revenueMapper.findCheckOutsByAdminAndDate(adminIdx, LocalDate.now());
        rows.forEach(this::markRequest);
        return rows;
    }

    private void markRequest(CheckInOutRowDto row) {
        row.setHasRequest(row.getResvRequest() != null && !row.getResvRequest().isBlank());
    }

    /** 희망 도착 시간(RESV_ARRIVAL_TIME, "15"~"19" 시 단위 문자열)이 현재 시각을 지났으면 완료로 본다. */
    private void markCompleted(CheckInOutRowDto row) {
        try {
            int arrivalHour = Integer.parseInt(row.getResvArrivalTime());
            row.setCompleted(arrivalHour <= LocalTime.now().getHour());
        } catch (NumberFormatException | NullPointerException e) {
            row.setCompleted(false);
        }
    }
}
