package com.mnu.ryokanmaker.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.mnu.ryokanmaker.domain.RoomDto;

@Mapper
public interface RoomMapper {
	public int insertRoom(RoomDto roomDto);
	public List<RoomDto> selectRoomsByAdmin(@Param("adminIdx") Integer adminIdx);
	public int updateRoom(RoomDto roomDto);
	public int deleteRoom(@Param("roomIdx") Integer roomIdx, @Param("adminIdx") Integer adminIdx);

	/** 사용자 예약 화면(객실 선택)에 노출할 판매중(SALE_YN='Y') 객실 전체 조회 */
	public List<RoomDto> findAllOnSale();

	/** 예약 시 선택한 방 상세 조회 */
	public RoomDto findById(@Param("roomIdx") Integer roomIdx);

	/** 판매중인 객실 전체 중 최저가 조회 -> 플랜 기준가로 사용 (특정 등급 고정 X) */
	public Long findMinPrice();

	/** 판매 여부(ROOM_SALE_YN)만 토글 */
	public int updateSaleYn(@Param("roomIdx") Integer roomIdx, @Param("adminIdx") Integer adminIdx,
			@Param("roomSaleYn") String roomSaleYn);
}
