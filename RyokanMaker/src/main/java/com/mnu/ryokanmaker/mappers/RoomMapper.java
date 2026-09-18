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

    // 판매중인 방 전체 중 최저가 조회 -> 플랜 가격에 포함된 기준 객실가로 사용 (특정 등급 고정 X)
    Long findMinPrice();

}
