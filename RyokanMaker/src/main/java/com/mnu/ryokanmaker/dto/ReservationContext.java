package com.mnu.ryokanmaker.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * /payment 진입 시 확정된 예약 선택값을 세션에 보관하는 객체.
 * 결제창을 띄우기 직전(/payment/prepare)에 이 값으로 RESERVATION / ROOM_RESERVATION을 저장한다.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationContext {

    private Integer adminIdx;
    private Integer planIdx;
    private Integer roomIdx;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private Integer totalAmount;
    private Integer people;
    private Integer courseIdx;
    private java.util.List<OnsenPickDto> onsenPicks;
}
