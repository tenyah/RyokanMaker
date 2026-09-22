package com.mnu.ryokanmaker.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.mnu.ryokanmaker.domain.FacilityDto;

@Mapper
public interface FacilityMapper {
	public int insertFacility(FacilityDto facilityDto);
	public List<FacilityDto> selectFacilitiesByAdmin(@Param("adminIdx") Integer adminIdx);
	public int updateFacility(FacilityDto facilityDto);
	public int deleteFacility(@Param("facilityIdx") Integer facilityIdx, @Param("adminIdx") Integer adminIdx);
}
