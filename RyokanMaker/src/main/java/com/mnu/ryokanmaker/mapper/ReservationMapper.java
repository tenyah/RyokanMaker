package com.mnu.ryokanmaker.mapper;

import com.mnu.ryokanmaker.domain.AdminReservationListItemDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** 마이페이지(회원 본인) 예약 현황 조회/회원탈퇴 시 예약 정리용 매퍼. */
@Mapper
public interface ReservationMapper {

    /** 특정 회원(userMail)의 예약 내역 전체 조회 (최신순). */
    List<AdminReservationListItemDto> selectReservationListByUserMail(@Param("userMail") String userMail);

    /*
     * 회원탈퇴 시 삭제 순서 (FK 때문에 순서 중요):
     * ROOM/ONSEN/RESTAURANT_RESERVATION -> RESERVATION -> (INQUIRY는 별도) -> MEMBER
     */
    int deleteRoomReservationsByUserMail(@Param("userMail") String userMail);
    int deleteOnsenReservationsByUserMail(@Param("userMail") String userMail);
    int deleteRestaurantReservationsByUserMail(@Param("userMail") String userMail);
    int deleteReservationsByUserMail(@Param("userMail") String userMail);
}
