package com.mnu.ryokanmaker.mappers;

import com.mnu.ryokanmaker.domain.RoomReservationDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDate;
import java.util.List;

@Mapper
public interface RoomReservationMapper {
    List<RoomReservationDTO> findOverlapping(@Param("rangeStart") LocalDate rangeStart,
                                              @Param("rangeEnd") LocalDate rangeEnd);
}