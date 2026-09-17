package com.mnu.ryokanmaker.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.mnu.ryokanmaker.domain.OnsenDto;

@Mapper
public interface OnsenMapper {
	public int insertOnsen(OnsenDto onsenDto);
	public List<OnsenDto> selectOnsensByAdmin(@Param("adminIdx") Integer adminIdx);
	public int updateOnsen(OnsenDto onsenDto);
	public int deleteOnsen(@Param("onsenIdx") Integer onsenIdx, @Param("adminIdx") Integer adminIdx);
}
