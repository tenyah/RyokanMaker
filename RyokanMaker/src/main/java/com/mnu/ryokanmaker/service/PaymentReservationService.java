package com.mnu.ryokanmaker.service;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mnu.ryokanmaker.domain.GuestInfoForm;
import com.mnu.ryokanmaker.domain.ReservationContext;
import com.mnu.ryokanmaker.domain.ReservationDto;
import com.mnu.ryokanmaker.domain.RoomReservationDto;
import com.mnu.ryokanmaker.mapper.PaymentMapper;

/**
 * 결제 흐름에서 예약 정보를 DB에 반영한다.
 * ROOM_RESERVATION.RESV_NUM이 RESERVATION을 FK로 참조하므로 RESERVATION을 먼저 저장해야 한다.
 */
@Service
public class PaymentReservationService {

    public static final String STATUS_RESERVED = "예약완료";
    public static final String PAY_STATUS_WAITING = "결제대기";
    public static final String PAY_STATUS_PAID = "결제완료";

    @Autowired
    private PaymentMapper paymentMapper;

    /** 결제창을 띄우기 직전에 '결제대기' 상태로 예약을 저장한다. */
    @Transactional
    public Integer saveAsWaiting(ReservationContext context, GuestInfoForm guestInfoForm, String userMail) {
        ReservationDto reservation = new ReservationDto();
        reservation.setAdminIdx(context.getAdminIdx());
        reservation.setUserMail(userMail);
        reservation.setResvPrice(context.getTotalAmount());
        reservation.setResvPeople(context.getPeople());
        reservation.setResvStatus(STATUS_RESERVED);
        reservation.setResvPayStatus(PAY_STATUS_WAITING);
        reservation.setResvArrivalTime(guestInfoForm.getArrivalTime());
        reservation.setResvRequest(guestInfoForm.getRequestNote());
        reservation.setResvDay(LocalDate.now());
        reservation.setResvOrderId(guestInfoForm.getOrderId());
        paymentMapper.insertReservation(reservation);

        RoomReservationDto roomReservation = new RoomReservationDto();
        roomReservation.setUserMail(userMail);
        roomReservation.setAdminIdx(context.getAdminIdx());
        roomReservation.setRoomIdx(context.getRoomIdx());
        roomReservation.setResvNum(reservation.getResvNum());
        roomReservation.setPlanIdx(context.getPlanIdx());
        roomReservation.setResvCheckIn(context.getCheckIn());
        roomReservation.setResvCheckOut(context.getCheckOut());
        roomReservation.setResvPrice(context.getTotalAmount());
        roomReservation.setResvPeople(context.getPeople());
        roomReservation.setResvStatus(STATUS_RESERVED);
        roomReservation.setResvPayStatus(PAY_STATUS_WAITING);
        paymentMapper.insertRoomReservation(roomReservation);

        return reservation.getResvNum();
    }

    /** 결제 승인이 끝난 뒤 RESERVATION과 ROOM_RESERVATION의 결제 상태/수단을 갱신한다. */
    @Transactional
    public void markAsPaid(String orderId, String payMethod) {
        paymentMapper.updatePayStatusByOrderId(orderId, PAY_STATUS_PAID, payMethod);

        Integer resvNum = paymentMapper.selectResvNumByOrderId(orderId);
        if (resvNum != null) {
            paymentMapper.updateRoomPayStatusByResvNum(resvNum, PAY_STATUS_PAID, payMethod);
        }
    }

    /** successUrl로 돌아온 amount 위변조 검증에 쓸 서버 신뢰 금액. */
    public Integer findTrustedAmount(String orderId) {
        return paymentMapper.selectResvPriceByOrderId(orderId);
    }
}
