package com.mnu.ryokanmaker.mappers;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.mnu.ryokanmaker.domain.RoomDTO;

@Mapper
public interface RoomMapper {
    List<RoomDTO> findAll();
    
    // 방 하나 상세 조회 (예약 시 선택한 방 정보 확인용)
    RoomDTO findById(@Param("roomIdx") Long roomIdx);

    // 특정 등급(roomLevel) 중 최저가 조회 -> 기준가(작은방)로 사용
    Long findMinPriceByLevel(@Param("roomLevel") String roomLevel);

}
