package com.mnu.ryokanmaker.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.mnu.ryokanmaker.domain.PlanDto;

@Mapper
public interface PlanMapper {
	public int insertPlan(PlanDto planDto);
	public List<PlanDto> selectPlansByAdmin(@Param("adminIdx") Integer adminIdx);
	public int updatePlan(PlanDto planDto);
	public int deletePlan(@Param("planIdx") Integer planIdx, @Param("adminIdx") Integer adminIdx);
}
