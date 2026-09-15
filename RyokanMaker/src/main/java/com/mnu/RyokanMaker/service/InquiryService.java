package com.mnu.RyokanMaker.service;

import com.mnu.RyokanMaker.dto.InquiryDto;
import com.mnu.RyokanMaker.mapper.InquiryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InquiryService {

    @Autowired
    private InquiryMapper inquiryMapper;

    /** 로그인한 회원이 작성한 문의 내역 (마이페이지 - 1:1 문의) */
    public List<InquiryDto> listByMember(String userMail) {
        return inquiryMapper.selectListByUserMail(userMail);
    }

    public InquiryDto select(int inquiryIdx) {
        return inquiryMapper.selectByIdx(inquiryIdx);
    }

    public int write(InquiryDto inquiryDto) {
        return inquiryMapper.insert(inquiryDto);
    }
}
