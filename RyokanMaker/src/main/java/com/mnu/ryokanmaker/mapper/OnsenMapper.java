package com.mnu.ryokanmaker.mapper;

import java.time.LocalDate;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.mnu.ryokanmaker.domain.OnsenDto;
import com.mnu.ryokanmaker.domain.OnsenPickDto;

@Mapper
public interface OnsenMapper {
	public int insertOnsen(OnsenDto onsenDto);
	public List<OnsenDto> selectOnsensByAdmin(@Param("adminIdx") Integer adminIdx);
	public int updateOnsen(OnsenDto onsenDto);
	public int deleteOnsen(@Param("onsenIdx") Integer onsenIdx, @Param("adminIdx") Integer adminIdx);

	/** 사용자 예약 화면(온천 선택)에 노출할 판매중(SALE_YN='Y') 온천 전체 조회 */
	public List<OnsenDto> findAllOnSale();

	/** [rangeStart, rangeEnd) 기간에 이미 예약된 온천 시간대 (취소된 예약 제외). */
	public List<OnsenPickDto> findReservedSlots(@Param("rangeStart") LocalDate rangeStart,
			@Param("rangeEnd") LocalDate rangeEnd);

	/** 판매 여부(ONSEN_SALE_YN)만 토글 */
	public int updateSaleYn(@Param("onsenIdx") Integer onsenIdx, @Param("adminIdx") Integer adminIdx,
			@Param("onsenSaleYn") String onsenSaleYn);
}
