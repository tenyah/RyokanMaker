package com.mnu.ryokanmaker.mapper;

import java.time.LocalDate;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.mnu.ryokanmaker.dto.RoomReservationDto;

@Mapper
public interface RoomReservationMapper {

	/** 주어진 기간과 겹치는 객실 예약 목록 (예약 가능 여부 판단용) */
	List<RoomReservationDto> findOverlapping(@Param("rangeStart") LocalDate rangeStart,
			@Param("rangeEnd") LocalDate rangeEnd);

	/** 특정 관리자의, 체크인 날짜가 [rangeStart, rangeEnd)에 속하는 객실 예약 목록 (매출 집계용). */
	List<RoomReservationDto> findByAdminAndCheckInRange(@Param("adminIdx") Integer adminIdx,
			@Param("rangeStart") LocalDate rangeStart, @Param("rangeEnd") LocalDate rangeEnd);
}
