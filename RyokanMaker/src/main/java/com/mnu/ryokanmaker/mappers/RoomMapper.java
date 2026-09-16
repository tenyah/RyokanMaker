package com.mnu.ryokanmaker.mappers;

import com.mnu.ryokanmaker.domain.RoomDTO;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface RoomMapper {
    List<RoomDTO> findAll();
}