package com.mnu.ryokanmaker.service;

import com.mnu.ryokanmaker.dto.InquiryDto;
import com.mnu.ryokanmaker.mapper.InquiryMapper;
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

    /** 문의 삭제 - 본인 글이 맞는지 확인 후 삭제. 본인 글이 아니거나 없으면 false. */
    public boolean delete(int inquiryIdx, String userMail) {
        InquiryDto inquiry = inquiryMapper.selectByIdx(inquiryIdx);
        if (inquiry == null || !inquiry.getUserMail().equals(userMail)) {
            return false;
        }
        inquiryMapper.deleteByIdx(inquiryIdx);
        return true;
    }
}
