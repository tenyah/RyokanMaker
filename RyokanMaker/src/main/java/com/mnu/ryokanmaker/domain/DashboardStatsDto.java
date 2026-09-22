package com.mnu.ryokanmaker.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 대시보드 최상단 KPI 카드용 오늘 운영 요약. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDto {

    private int roomsTotal;
    private int roomsOccupiedToday;
    private int occupancyRate;

    private int checkInsToday;
    private int checkInsCompleted;
    private int checkOutsToday;

    private int todayRevenue;
    private int monthToDateRevenue;
}
