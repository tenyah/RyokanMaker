package com.mnu.ryokanmaker.service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mnu.ryokanmaker.domain.AdminPlanDto;
import com.mnu.ryokanmaker.domain.MonthlyTrendPointDto;
import com.mnu.ryokanmaker.domain.PlanSettlementDto;
import com.mnu.ryokanmaker.domain.RevenueDayDto;
import com.mnu.ryokanmaker.domain.RevenueMonthSummaryDto;
import com.mnu.ryokanmaker.domain.RevenueTxnDto;
import com.mnu.ryokanmaker.domain.RoomDto;
import com.mnu.ryokanmaker.domain.RoomReservationDto;
import com.mnu.ryokanmaker.domain.RoomShareDto;
import com.mnu.ryokanmaker.mapper.PlanMapper;
import com.mnu.ryokanmaker.mapper.RevenueMapper;
import com.mnu.ryokanmaker.mapper.RoomMapper;
import com.mnu.ryokanmaker.mapper.RoomReservationMapper;

/**
 * 관리자 - 매출현황(월매출조회 / 일자별 매출조회) · 대시보드 공용 집계 서비스.
 * 모든 집계는 체크인 날짜 기준이며(ROOM_RESERVATION.RESV_CHECK_IN), 매출로 잡는 것은
 * 결제완료(RESV_PAY_STATUS='결제완료') 건만이다.
 */
@Service
public class RevenueService {

    public static final String PAID = "결제완료";

    @Autowired
    private RoomReservationMapper roomReservationMapper;

    @Autowired
    private RevenueMapper revenueMapper;

    @Autowired
    private RoomMapper roomMapper;

    @Autowired
    private PlanMapper planMapper;

    /** 체크인 날짜가 [start, end)인 객실 예약 원본 목록 (결제완료/결제대기 모두 포함). */
    public List<RoomReservationDto> fetchRange(Integer adminIdx, LocalDate start, LocalDate end) {
        return roomReservationMapper.findByAdminAndCheckInRange(adminIdx, start, end);
    }

    public List<RoomReservationDto> paidOnly(List<RoomReservationDto> rows) {
        return rows.stream().filter(r -> PAID.equals(r.getResvPayStatus())).toList();
    }

    private long nightsSold(List<RoomReservationDto> paidRows) {
        return paidRows.stream()
                .mapToLong(r -> ChronoUnit.DAYS.between(r.getResvCheckIn(), r.getResvCheckOut()))
                .sum();
    }

    public int totalRevenue(List<RoomReservationDto> paidRows) {
        return paidRows.stream().mapToInt(RoomReservationDto::getResvPrice).sum();
    }

    /** 객실 가동률(%) = 판매된 박수 / (전체 객실수 × 기간 일수). 체크인 기준 집계라 실 재실률의 근사치다. */
    public int occupancyRate(Integer adminIdx, List<RoomReservationDto> paidRows, LocalDate start, LocalDate end) {
        int roomsTotal = roomMapper.selectRoomsByAdmin(adminIdx).size();
        long daysInPeriod = ChronoUnit.DAYS.between(start, end);
        if (roomsTotal == 0 || daysInPeriod <= 0) {
            return 0;
        }
        long nights = nightsSold(paidRows);
        return (int) Math.round(nights * 100.0 / (roomsTotal * daysInPeriod));
    }

    /** 평균 객실 단가(ADR) = 결제완료 매출 합계 / 판매된 박수. */
    public int adr(List<RoomReservationDto> paidRows) {
        long nights = nightsSold(paidRows);
        if (nights == 0) {
            return 0;
        }
        return (int) Math.round(totalRevenue(paidRows) / (double) nights);
    }

    /** 월매출조회 화면 한 달치 데이터 (캘린더 + 요일별 합계 + KPI). */
    public RevenueMonthSummaryDto getMonthSummary(Integer adminIdx, YearMonth ym) {
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.plusMonths(1).atDay(1);

        List<RoomReservationDto> paid = paidOnly(fetchRange(adminIdx, start, end));

        int daysInMonth = ym.lengthOfMonth();
        int[] dailyTotals = new int[daysInMonth + 1];
        for (RoomReservationDto r : paid) {
            dailyTotals[r.getResvCheckIn().getDayOfMonth()] += r.getResvPrice();
        }

        LocalDate today = LocalDate.now();
        List<RevenueDayDto> days = new ArrayList<>();
        int[] dowTotals = new int[7];
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = ym.atDay(day);
            boolean future = date.isAfter(today);
            int amount = dailyTotals[day];
            days.add(new RevenueDayDto(date, amount, future));
            if (!future) {
                // DayOfWeek.getValue(): MON=1..SUN=7 → 일요일을 0으로 두는 인덱스로 변환
                dowTotals[date.getDayOfWeek().getValue() % 7] += amount;
            }
        }
        List<Integer> dowList = new ArrayList<>();
        for (int v : dowTotals) {
            dowList.add(v);
        }

        int startPad = start.getDayOfWeek().getValue() % 7;

        return new RevenueMonthSummaryDto(ym, totalRevenue(paid),
                occupancyRate(adminIdx, paid, start, end), adr(paid), startPad, days, dowList);
    }

    /** 월별 매출 추이 차트 (해당 연도 vs 전년, 1~12월). 아직 도래하지 않은 달은 null. */
    public List<MonthlyTrendPointDto> getMonthlyTrend(Integer adminIdx, int year) {
        LocalDate curStart = LocalDate.of(year, 1, 1);
        LocalDate curEnd = LocalDate.of(year + 1, 1, 1);
        LocalDate prevStart = LocalDate.of(year - 1, 1, 1);
        LocalDate prevEnd = LocalDate.of(year, 1, 1);

        Map<Integer, Integer> curByMonth = groupByMonth(paidOnly(fetchRange(adminIdx, curStart, curEnd)));
        Map<Integer, Integer> prevByMonth = groupByMonth(paidOnly(fetchRange(adminIdx, prevStart, prevEnd)));

        YearMonth currentYm = YearMonth.now();
        List<MonthlyTrendPointDto> points = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            Integer curAmount = YearMonth.of(year, m).isAfter(currentYm) ? null : curByMonth.getOrDefault(m, 0);
            Integer prevAmount = prevByMonth.getOrDefault(m, 0);
            points.add(new MonthlyTrendPointDto(m, curAmount, prevAmount));
        }
        return points;
    }

    private Map<Integer, Integer> groupByMonth(List<RoomReservationDto> paidRows) {
        Map<Integer, Integer> map = new HashMap<>();
        for (RoomReservationDto r : paidRows) {
            map.merge(r.getResvCheckIn().getMonthValue(), r.getResvPrice(), Integer::sum);
        }
        return map;
    }

    /** 객실별 매출 비중 (금액 내림차순). */
    public List<RoomShareDto> getRoomShare(Integer adminIdx, LocalDate start, LocalDate end) {
        List<RoomReservationDto> paid = paidOnly(fetchRange(adminIdx, start, end));
        Map<Integer, Integer> byRoom = new LinkedHashMap<>();
        for (RoomReservationDto r : paid) {
            byRoom.merge(r.getRoomIdx(), r.getResvPrice(), Integer::sum);
        }
        int total = byRoom.values().stream().mapToInt(Integer::intValue).sum();

        Map<Integer, String> roomNames = new HashMap<>();
        for (RoomDto room : roomMapper.selectRoomsByAdmin(adminIdx)) {
            roomNames.put(room.getRoomIdx(), room.getRoomName());
        }

        List<RoomShareDto> result = new ArrayList<>();
        for (Map.Entry<Integer, Integer> e : byRoom.entrySet()) {
            int pct = total == 0 ? 0 : (int) Math.round(e.getValue() * 100.0 / total);
            result.add(new RoomShareDto(roomNames.getOrDefault(e.getKey(), "-"), e.getValue(), pct));
        }
        result.sort((a, b) -> b.getAmount() - a.getAmount());
        return result;
    }

    /** 플랜별 매출 정산 (금액 내림차순). 플랜 없이 예약된 건은 제외한다. */
    public List<PlanSettlementDto> getPlanSettlement(Integer adminIdx, LocalDate start, LocalDate end) {
        List<RoomReservationDto> paid = paidOnly(fetchRange(adminIdx, start, end));
        Map<Integer, Integer> byPlan = new LinkedHashMap<>();
        for (RoomReservationDto r : paid) {
            if (r.getPlanIdx() == null) {
                continue;
            }
            byPlan.merge(r.getPlanIdx(), r.getResvPrice(), Integer::sum);
        }

        Map<Integer, String> planNames = new HashMap<>();
        for (AdminPlanDto plan : planMapper.selectPlansByAdmin(adminIdx)) {
            planNames.put(plan.getPlanIdx(), plan.getPlanName());
        }

        List<PlanSettlementDto> result = new ArrayList<>();
        for (Map.Entry<Integer, Integer> e : byPlan.entrySet()) {
            result.add(new PlanSettlementDto(planNames.getOrDefault(e.getKey(), "-"), e.getValue()));
        }
        result.sort((a, b) -> b.getAmount() - a.getAmount());
        return result;
    }

    /** 일자별 매출조회 화면의 거래 목록 (고객명/객실명/플랜명 포함, 결제완료+결제대기 모두). */
    public List<RevenueTxnDto> getTransactions(Integer adminIdx, LocalDate start, LocalDate end) {
        return revenueMapper.findTxnsByAdminAndCheckInRange(adminIdx, start, end);
    }
}
