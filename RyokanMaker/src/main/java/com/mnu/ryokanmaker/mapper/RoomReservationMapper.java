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

	/** 해당 객실에 date 날짜에 걸친 예약(RESERVATION)의 상태를 fromStatus일 때만 toStatus로 변경 */
	int updateResvStatusByRoomAndDate(@Param("adminIdx") Integer adminIdx, @Param("roomIdx") Integer roomIdx,
			@Param("date") LocalDate date, @Param("fromStatus") String fromStatus, @Param("toStatus") String toStatus);

	/** 해당 객실에 date 날짜에 걸친 객실예약(ROOM_RESERVATION)의 상태를 fromStatus일 때만 toStatus로 변경 */
	int updateRoomResvStatusByRoomAndDate(@Param("adminIdx") Integer adminIdx, @Param("roomIdx") Integer roomIdx,
			@Param("date") LocalDate date, @Param("fromStatus") String fromStatus, @Param("toStatus") String toStatus);
}
