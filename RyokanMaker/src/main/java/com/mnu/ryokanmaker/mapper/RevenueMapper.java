package com.mnu.ryokanmaker.mapper;

import java.time.LocalDate;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.mnu.ryokanmaker.domain.CheckInOutRowDto;
import com.mnu.ryokanmaker.domain.RevenueTxnDto;

/**
 * 매출현황(일자별 매출조회)·대시보드에서 쓰는, 객실 예약에 고객명/객실명/플랜명을 조인한 조회.
 * 집계용 숫자 계산은 RoomReservationMapper.findByAdminAndCheckInRange로 따로 처리한다.
 */
@Mapper
public interface RevenueMapper {

	/** 체크인 날짜가 [rangeStart, rangeEnd)인 거래 목록 (고객명/객실명/플랜명 포함). */
	List<RevenueTxnDto> findTxnsByAdminAndCheckInRange(@Param("adminIdx") Integer adminIdx,
			@Param("rangeStart") LocalDate rangeStart, @Param("rangeEnd") LocalDate rangeEnd);

	/** 오늘 체크인하는 예약 목록 (대시보드용). */
	List<CheckInOutRowDto> findCheckInsByAdminAndDate(@Param("adminIdx") Integer adminIdx,
			@Param("date") LocalDate date);

	/** 오늘 체크아웃하는 예약 목록 (대시보드용). */
	List<CheckInOutRowDto> findCheckOutsByAdminAndDate(@Param("adminIdx") Integer adminIdx,
			@Param("date") LocalDate date);
}
