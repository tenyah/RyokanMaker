package com.mnu.ryokanmaker.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mnu.ryokanmaker.domain.AdminPlanDto;
import com.mnu.ryokanmaker.domain.BathAvailabilityDto;
import com.mnu.ryokanmaker.domain.OnsenDayDto;
import com.mnu.ryokanmaker.domain.OnsenDto;
import com.mnu.ryokanmaker.domain.OnsenPickDto;
import com.mnu.ryokanmaker.domain.PriceBreakdownDto;
import com.mnu.ryokanmaker.domain.RestaurantCourseDto;
import com.mnu.ryokanmaker.domain.RoomAvailabilityDto;
import com.mnu.ryokanmaker.domain.RoomDto;
import com.mnu.ryokanmaker.domain.RoomReservationDto;
import com.mnu.ryokanmaker.domain.SlotDto;
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
        List<RestaurantCourseDto> courses = restaurantCourseMapper.findAllOnSale();
        int basePrice = courses.stream().mapToInt(c -> priceOf(c.getRestaurantCoursePrice())).min().orElse(0);
        for (RestaurantCourseDto c : courses) {
            c.setExtraCharge(priceOf(c.getRestaurantCoursePrice()) - basePrice);
        }
        courses.sort(Comparator.comparingInt((RestaurantCourseDto c) -> priceOf(c.getRestaurantCoursePrice()))
                .thenComparing(RestaurantCourseDto::getRestaurantCourseIdx));
        return courses;
    }

    private int priceOf(Integer price) {
        return price == null ? 0 : price;
    }

    public List<RoomAvailabilityDto> getRoomAvailability(LocalDate checkIn, LocalDate checkOut,
                                                          int adultCount, int childCount) {
        int requestedPeople = adultCount + childCount;
        List<RoomReservationDto> reserved = roomReservationMapper.findOverlapping(checkIn, checkOut);

        Long basePrice = roomMapper.findMinPrice(); // 현재 판매중인 방 중 최저가 — 플랜 가격에 이미 포함된 금액
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
    /** 숙박 기간의 매일(체크인 ~ 체크아웃 전날)마다 온천/시간대 선택표를 만든다. */
    public List<OnsenDayDto> getOnsenDays(LocalDate checkIn, LocalDate checkOut) {
        List<OnsenDto> onsenList = onsenMapper.findAllOnSale();
        List<OnsenDayDto> days = new ArrayList<>();
        for (LocalDate date : checkIn.datesUntil(checkOut).toList()) {
            days.add(new OnsenDayDto(date, buildBaths(onsenList, date)));
        }
        return days;
    }

    private List<BathAvailabilityDto> buildBaths(List<OnsenDto> onsenList, LocalDate date) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");
        int onsenBasePrice = onsenList.stream().mapToInt(o -> priceOf(o.getOnsenPrice())).min().orElse(0);
        int dayShift = (int) (date.toEpochDay() % 3);

        // 표 헤더는 하나라서, 모든 온천의 1시간 단위 시작시각을 합쳐서 공통 컬럼으로 사용
        TreeSet<LocalTime> allTimes = new TreeSet<>();
        for (OnsenDto onsen : onsenList) {
            allTimes.addAll(hourlySlots(onsen));
        }

        List<BathAvailabilityDto> baths = new ArrayList<>();
        for (int i = 0; i < onsenList.size(); i++) {
            OnsenDto onsen = onsenList.get(i);
            List<LocalTime> ownTimes = hourlySlots(onsen);
            List<SlotDto> slots = new ArrayList<>();
            int t = 0;
            for (LocalTime time : allTimes) {
                // 해당 온천 이용시간 밖이면 마감 처리. 이용시간 안의 예약여부는 아직 목업
                boolean available = ownTimes.contains(time) && (i + t + dayShift) % 3 != 0;
                slots.add(new SlotDto(time.format(fmt), available));
                t++;
            }
            baths.add(new BathAvailabilityDto(onsen.getOnsenIdx(), onsen.getOnsenName(), slots,
                    priceOf(onsen.getOnsenPrice()) - onsenBasePrice));
        }
        return baths;
    }

    /** 관리자가 입력한 이용시간(ONSEN_HOUR "시작–종료")을 1시간 단위 시작시각 목록으로 분할. 종료시각은 제외. */
    private List<LocalTime> hourlySlots(OnsenDto onsen) {
        List<LocalTime> result = new ArrayList<>();
        try {
            LocalTime start = LocalTime.parse(onsen.getOnsenStartTime());
            LocalTime end = LocalTime.parse(onsen.getOnsenEndTime());
            for (LocalTime t = start; t.isBefore(end); t = t.plusHours(1)) {
                result.add(t);
            }
        } catch (DateTimeParseException e) {
            // 이용시간 미입력/형식 오류 -> 슬롯 없음
        }
        return result;
    }

    /**
     * 최종 결제금액 = 플랜가격
     *   + (선택한 방 가격 - 판매중 방 최저가)
     *   + (선택한 코스 가격 - 판매중 코스 최저가) x 숙박 일수   ※ courseIdx가 있을 때만
     *   + 날짜별로 고른 온천마다 (온천 가격 - 판매중 온천 최저가)
     */
    public Integer calculateFinalPrice(Integer planIdx, Integer roomIdx, Integer courseIdx, List<OnsenPickDto> onsenPicks, int nights) {
        return calculatePriceBreakdown(planIdx, roomIdx, courseIdx, onsenPicks, nights).getTotal();
    }

    /** 결제금액을 플랜 요금 / 객실 추가 / 코스 추가 / 온천 추가로 나눠서 계산한다. */
    /** 코스 추가요금은 1박 기준 차액 x 숙박 일수. */
    public PriceBreakdownDto calculatePriceBreakdown(Integer planIdx, Integer roomIdx, Integer courseIdx, List<OnsenPickDto> onsenPicks, int nights) {
        AdminPlanDto plan = planMapper.findById(planIdx);
        RoomDto room = roomMapper.findById(roomIdx);

        Long basePrice = roomMapper.findMinPrice();
        if (basePrice == null) basePrice = 0L;

        int planFee = plan.getPlanPrice().intValue();
        int roomExtra = (int) Math.max(0, room.getRoomPrice() - basePrice);

        Integer courseExtra = null;
        if (courseIdx != null) {
            courseExtra = getCourses().stream()
                    .filter(c -> courseIdx.equals(c.getRestaurantCourseIdx()))
                    .mapToInt(RestaurantCourseDto::getExtraCharge).findFirst().orElse(0) * nights;
        }

        Integer onsenExtra = null;
        if (onsenPicks != null && !onsenPicks.isEmpty()) {
            List<OnsenDto> onsenList = onsenMapper.findAllOnSale();
            int onsenBasePrice = onsenList.stream().mapToInt(o -> priceOf(o.getOnsenPrice())).min().orElse(0);
            int sum = 0;
            for (OnsenPickDto pick : onsenPicks) {
                sum += onsenList.stream()
                        .filter(o -> o.getOnsenIdx().equals(pick.getOnsenIdx()))
                        .mapToInt(o -> priceOf(o.getOnsenPrice()) - onsenBasePrice).findFirst().orElse(0);
            }
            onsenExtra = sum;
        }
        return new PriceBreakdownDto(planFee, roomExtra, courseExtra, onsenExtra);
    }
    /**
     * 화면에서 넘어온 "날짜|온천idx|시간" 문자열 목록을 파싱한다.
     * 형식이 틀리거나 숙박 기간(체크인 ~ 체크아웃 전날) 밖의 날짜는 버린다.
     */
    public List<OnsenPickDto> parseOnsenPicks(List<String> raw, LocalDate checkIn, LocalDate checkOut) {
        List<OnsenPickDto> picks = new ArrayList<>();
        if (raw == null) return picks;
        for (String s : raw) {
            String[] p = s.split("\\|");
            if (p.length != 3) continue;
            try {
                LocalDate date = LocalDate.parse(p[0]);
                if (date.isBefore(checkIn) || !date.isBefore(checkOut)) continue;
                picks.add(new OnsenPickDto(date, Integer.valueOf(p[1]), p[2]));
            } catch (RuntimeException e) {
                // 잘못된 값은 무시
            }
        }
        return picks;
    }
}
