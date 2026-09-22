package com.mnu.ryokanmaker.service;

import com.mnu.ryokanmaker.dto.AdminReservationDetailDto;
import com.mnu.ryokanmaker.dto.AdminReservationListItemDto;
import com.mnu.ryokanmaker.dto.MemberDto;
import com.mnu.ryokanmaker.dto.ReservationDto;
import com.mnu.ryokanmaker.mapper.AdminReservationMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/** 관리자 - 예약 현황(admin_reservation.html) 화면용 조회 서비스. */
@Service
public class AdminReservationService {

    private final AdminReservationMapper adminReservationMapper;

    public AdminReservationService(AdminReservationMapper adminReservationMapper) {
        this.adminReservationMapper = adminReservationMapper;
    }

    public List<AdminReservationListItemDto> getReservationList(Integer adminIdx) {
        return adminReservationMapper.selectReservationList(adminIdx);
    }

    /** 예약 1건의 상세 패널 데이터를 RESERVATION 기준으로 조립한다. */
    public AdminReservationDetailDto getReservationDetail(Integer resvNum) {
        ReservationDto header = adminReservationMapper.selectReservationHeader(resvNum);
        if (header == null) {
            return null;
        }

        AdminReservationDetailDto detail = new AdminReservationDetailDto();
        detail.setResvNum(header.getResvNum());
        detail.setResvDay(header.getResvDay());
        detail.setResvStatus(header.getResvStatus());
        detail.setUserMail(header.getUserMail());
        MemberDto member = adminReservationMapper.selectMember(header.getUserMail());
        if (member != null) {
            detail.setUserNickname(member.getUserNickname());
            detail.setUserTel(member.getUserTel());
            detail.setUserCountry(member.getUserCountry());
        }
        detail.setResvArrivalTime(header.getResvArrivalTime());
        detail.setResvPeople(header.getResvPeople());
        detail.setResvPrice(header.getResvPrice());
        detail.setResvPayStatus(header.getResvPayStatus());
        detail.setResvPayMethod(header.getResvPayMethod());
        detail.setResvRequest(header.getResvRequest());

        detail.setRooms(adminReservationMapper.selectRoomItems(resvNum));
        detail.setRestaurants(adminReservationMapper.selectRestaurantItems(resvNum));
        detail.setOnsens(adminReservationMapper.selectOnsenItems(resvNum));

        return detail;
    }
}
