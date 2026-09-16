package com.mnu.ryokanmaker.mappers;

import com.mnu.ryokanmaker.domain.PlanDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PlanMapper {
    List<PlanDTO> findAll();
}