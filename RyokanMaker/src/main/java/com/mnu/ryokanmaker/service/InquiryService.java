package com.mnu.ryokanmaker.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mnu.ryokanmaker.domain.InquiryDto;
import com.mnu.ryokanmaker.mapper.InquiryMapper;

@Service
public class InquiryService {

	@Autowired
	private InquiryMapper inquiryMapper;

	/** status가 null이면 전체 조회 */
	public List<InquiryDto> getInquiryList(Integer adminIdx, String status) {
		return inquiryMapper.selectInquiriesByAdmin(adminIdx, status);
	}

	public InquiryDto getInquiry(Integer inquiryIdx, Integer adminIdx) {
		return inquiryMapper.selectInquiry(inquiryIdx, adminIdx);
	}

	public int countTotal(Integer adminIdx) {
		return inquiryMapper.countByStatus(adminIdx, null);
	}

	public int countByStatus(Integer adminIdx, String status) {
		return inquiryMapper.countByStatus(adminIdx, status);
	}

	/**
	 * 관리자 답변 등록. 본인(adminIdx) 소유 문의가 아니면 아무것도 갱신하지 않는다.
	 */
	public boolean answerInquiry(Integer inquiryIdx, Integer adminIdx, String answerContent) {
		InquiryDto inquiryDto = new InquiryDto();
		inquiryDto.setInquiryIdx(inquiryIdx);
		inquiryDto.setAdminIdx(adminIdx);
		inquiryDto.setInquiryAnswerContent(answerContent);
		return inquiryMapper.answerInquiry(inquiryDto) > 0;
	}
}
