package com.mnu.ryokanmaker.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.mnu.ryokanmaker.domain.InquiryDto;

@Mapper
public interface InquiryMapper {
	public List<InquiryDto> selectInquiriesByAdmin(@Param("adminIdx") Integer adminIdx, @Param("status") String status);
	public InquiryDto selectInquiry(@Param("inquiryIdx") Integer inquiryIdx, @Param("adminIdx") Integer adminIdx);
	public int answerInquiry(InquiryDto inquiryDto);
	public int countByStatus(@Param("adminIdx") Integer adminIdx, @Param("status") String status);
}
