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

	/** 사용자 예약 화면(플랜 선택)에 노출할 판매중(SALE_YN='Y') 플랜 전체 조회 */
	public List<AdminPlanDto> findAllOnSale();

	/** 예약 시 선택한 플랜 상세 조회 */
	public AdminPlanDto findById(@Param("planIdx") Integer planIdx);
}
