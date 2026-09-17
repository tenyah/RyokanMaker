package com.mnu.ryokanmaker.mappers;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.mnu.ryokanmaker.domain.PlanDTO;

@Mapper
public interface PlanMapper {
    List<PlanDTO> findAll();
    
    PlanDTO findById(@Param("planIdx") Long planIdx);
}