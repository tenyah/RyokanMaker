package com.mnu.ryokanmaker.mapper;

import com.mnu.ryokanmaker.dto.ReservationDto;
import com.mnu.ryokanmaker.dto.RoomReservationDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** 결제(payment.html) 흐름에서 RESERVATION / ROOM_RESERVATION에 예약·결제 정보를 반영하는 매퍼. */
@Mapper
public interface PaymentMapper {

    /** RESERVATION 1건 저장. resvNum은 시퀀스로 채번되어 dto에 세팅된다. */
    void insertReservation(ReservationDto reservationDto);

    /** ROOM_RESERVATION 1건 저장. roomResvNum은 시퀀스로 채번되어 dto에 세팅된다. */
    void insertRoomReservation(RoomReservationDto roomReservationDto);

    /** 결제 승인(confirm) 성공 후 Resv_Order_Id 기준으로 RESERVATION의 결제 상태/수단을 갱신. */
    void updatePayStatusByOrderId(@Param("resvOrderId") String resvOrderId,
                                   @Param("resvPayStatus") String resvPayStatus,
                                   @Param("resvPayMethod") String resvPayMethod);

    /** RESERVATION과 같은 건의 ROOM_RESERVATION 결제 상태/수단도 함께 갱신. */
    void updateRoomPayStatusByResvNum(@Param("resvNum") Integer resvNum,
                                       @Param("resvPayStatus") String resvPayStatus,
                                       @Param("resvPayMethod") String resvPayMethod);

    /** 주문번호로 예약번호 조회 (ROOM_RESERVATION 갱신용). */
    Integer selectResvNumByOrderId(@Param("resvOrderId") String resvOrderId);

    /** successUrl에서 돌아온 amount 위변조 여부를 대조하기 위한 서버 신뢰 금액 조회. */
    Integer selectResvPriceByOrderId(@Param("resvOrderId") String resvOrderId);
}
