package com.mnu.ryokanmaker.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mnu.ryokanmaker.domain.AdminPlanDto;
import com.mnu.ryokanmaker.domain.PlanDayStatusDto;
import com.mnu.ryokanmaker.domain.PlanSalesRowDto;
import com.mnu.ryokanmaker.domain.RoomReservationDto;
import com.mnu.ryokanmaker.mapper.PlanMapper;
import com.mnu.ryokanmaker.mapper.RoomReservationMapper;

/** 관리자 - 판매 관리(plan_sales.html) 표 조회 서비스. */
@Service
public class PlanSalesService {

    @Autowired
    private PlanMapper planMapper;

    @Autowired
    private RoomReservationMapper roomReservationMapper;

    /** rangeStart부터 days일간, 로그인한 관리자의 플랜별 날짜별(체크인 기준) 예약 건수를 계산한다. */
    public List<PlanSalesRowDto> getSalesGrid(Integer adminIdx, LocalDate rangeStart, int days) {
        LocalDate rangeEnd = rangeStart.plusDays(days);

        List<AdminPlanDto> plans = planMapper.selectPlansByAdmin(adminIdx);
        List<RoomReservationDto> reserved = roomReservationMapper.findOverlapping(rangeStart, rangeEnd);

        List<PlanSalesRowDto> grid = new ArrayList<>();
        for (AdminPlanDto plan : plans) {
            List<PlanDayStatusDto> dayStatuses = new ArrayList<>();
            for (int i = 0; i < days; i++) {
                LocalDate day = rangeStart.plusDays(i);
                long count = reserved.stream()
                        .filter(r -> plan.getPlanIdx().equals(r.getPlanIdx()))
                        .filter(r -> day.equals(r.getResvCheckIn()))
                        .count();
                dayStatuses.add(new PlanDayStatusDto(day, count));
            }
            grid.add(new PlanSalesRowDto(plan, dayStatuses));
        }
        return grid;
    }
}
