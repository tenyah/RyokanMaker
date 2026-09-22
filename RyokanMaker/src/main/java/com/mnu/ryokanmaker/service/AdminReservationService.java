package com.mnu.ryokanmaker.service;

import com.mnu.ryokanmaker.domain.AdminReservationDetailDto;
import com.mnu.ryokanmaker.domain.AdminReservationListItemDto;
import com.mnu.ryokanmaker.domain.MemberDto;
import com.mnu.ryokanmaker.domain.ReservationDto;
import com.mnu.ryokanmaker.mapper.AdminReservationMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/** 관리자 - 예약 현황(admin_reservation.html) 화면용 조회 서비스. */
@Service
public class AdminReservationService {

    private final AdminReservationMapper adminReservationMapper;

    public AdminReservationService(AdminReservationMapper adminReservationMapper) {
        this.adminReservationMapper = adminReservationMapper;
    }

    /** 조건이 null이면 그 조건은 적용하지 않는다 (둘 다 null이면 전체). */
    public List<AdminReservationListItemDto> getReservationList(Integer adminIdx, LocalDate checkInDate, String keyword) {
        return adminReservationMapper.selectReservationList(adminIdx, checkInDate, keyword);
    }

    /**
     * 예약 취소. 기록은 남기고 상태를 '예약취소'로 바꾼다.
     * 토스 환불은 하지 않는다 — 문서용 공용 테스트 키라 토스의 결제 조회·취소 API를 쓸 수 없고,
     * 테스트 결제라 실제로 빠져나간 돈도 없다. 결제완료 건은 결제 상태만 '결제취소'로 표시한다.
     */
    @Transactional
    public void cancelReservation(Integer resvNum, Integer adminIdx) {
        ReservationDto header = adminReservationMapper.selectReservationHeader(resvNum);
        if (header == null || !adminIdx.equals(header.getAdminIdx())) {
            throw new IllegalArgumentException("예약을 찾을 수 없습니다: resvNum=" + resvNum);
        }
        if (PaymentReservationService.STATUS_CANCELLED.equals(header.getResvStatus())) {
            throw new IllegalStateException("이미 취소된 예약입니다: resvNum=" + resvNum);
        }

        String payStatus = header.getResvPayStatus();
        if (PaymentReservationService.PAY_STATUS_PAID.equals(payStatus)) {
            payStatus = PaymentReservationService.PAY_STATUS_CANCELLED;
        }

        String cancelled = PaymentReservationService.STATUS_CANCELLED;
        adminReservationMapper.cancelReservation(resvNum, adminIdx, cancelled, payStatus);
        adminReservationMapper.cancelRoomReservations(resvNum, adminIdx, cancelled, payStatus);
        adminReservationMapper.cancelOnsenReservations(resvNum, adminIdx, cancelled);
    }

    public int countTodayCheckIns(Integer adminIdx) {
        return adminReservationMapper.countTodayCheckIns(adminIdx);
    }

    public int countTodayCheckOuts(Integer adminIdx) {
        return adminReservationMapper.countTodayCheckOuts(adminIdx);
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
        }
        detail.setResvArrivalTime(header.getResvArrivalTime());
        detail.setResvLastNameEn(header.getResvLastNameEn());
        detail.setResvFirstNameEn(header.getResvFirstNameEn());
        detail.setResvLastNameJp(header.getResvLastNameJp());
        detail.setResvFirstNameJp(header.getResvFirstNameJp());
        detail.setResvMail(header.getResvMail());
        detail.setResvCountry(header.getResvCountry());
        detail.setResvTel(header.getResvTel());
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
