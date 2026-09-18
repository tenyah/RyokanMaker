package com.mnu.ryokanmaker.mapper;

import java.time.LocalDate;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.mnu.ryokanmaker.domain.RoomReservationDto;

@Mapper
public interface RoomReservationMapper {

	/** 주어진 기간과 겹치는 객실 예약 목록 (예약 가능 여부 판단용) */
	List<RoomReservationDto> findOverlapping(@Param("rangeStart") LocalDate rangeStart,
			@Param("rangeEnd") LocalDate rangeEnd);
}
