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
}
