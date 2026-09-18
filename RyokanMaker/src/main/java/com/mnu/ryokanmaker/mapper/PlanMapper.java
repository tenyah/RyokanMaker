package com.mnu.ryokanmaker.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.mnu.ryokanmaker.dto.AdminPlanDto;

@Mapper
public interface PlanMapper {
	public int insertPlan(AdminPlanDto planDto);
	public List<AdminPlanDto> selectPlansByAdmin(@Param("adminIdx") Integer adminIdx);
	public int updatePlan(AdminPlanDto planDto);
	public int deletePlan(@Param("planIdx") Integer planIdx, @Param("adminIdx") Integer adminIdx);
}
