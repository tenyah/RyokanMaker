package com.mnu.ryokanmaker.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.mnu.ryokanmaker.dto.OnsenDto;

@Mapper
public interface OnsenMapper {
	public int insertOnsen(OnsenDto onsenDto);
	public List<OnsenDto> selectOnsensByAdmin(@Param("adminIdx") Integer adminIdx);
	public int updateOnsen(OnsenDto onsenDto);
	public int deleteOnsen(@Param("onsenIdx") Integer onsenIdx, @Param("adminIdx") Integer adminIdx);

	/** 사용자 예약 화면(온천 선택)에 노출할 판매중(SALE_YN='Y') 온천 전체 조회 */
	public List<OnsenDto> findAllOnSale();
}
