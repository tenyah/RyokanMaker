package com.mnu.ryokanmaker.mapper;

import com.mnu.ryokanmaker.domain.AdminReservationListItemDto;
import com.mnu.ryokanmaker.domain.AdminReservationOnsenItemDto;
import com.mnu.ryokanmaker.domain.AdminReservationRestaurantItemDto;
import com.mnu.ryokanmaker.domain.AdminReservationRoomItemDto;
import com.mnu.ryokanmaker.domain.MemberDto;
import com.mnu.ryokanmaker.domain.ReservationDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** 관리자 - 예약 현황(admin_reservation.html) 화면 전용 조회 매퍼. */
@Mapper
public interface AdminReservationMapper {

    /** 왼쪽 예약 목록 카드용 (RESERVATION 기준, 대표 객실 1건만 조인). */
    List<AdminReservationListItemDto> selectReservationList(@Param("adminIdx") Integer adminIdx);

    /** 상세 패널 상단(예약자/예약 정보)의 기준이 되는 RESERVATION + MEMBER 조인 결과. */
    ReservationDto selectReservationHeader(@Param("resvNum") Integer resvNum);

    /** 상세 패널의 예약자 정보(닉네임/국적/연락처 등) 표시용. */
    MemberDto selectMember(@Param("userMail") String userMail);

    List<AdminReservationRoomItemDto> selectRoomItems(@Param("resvNum") Integer resvNum);

    List<AdminReservationRestaurantItemDto> selectRestaurantItems(@Param("resvNum") Integer resvNum);

    List<AdminReservationOnsenItemDto> selectOnsenItems(@Param("resvNum") Integer resvNum);
}
