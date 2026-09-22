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

    @Autowired
    private EmailService emailService;

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

    /** status가 null이면 전체 조회 (관리자 문의 목록) */
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
        InquiryDto before = inquiryMapper.selectInquiry(inquiryIdx, adminIdx);
        boolean firstAnswer = before != null
                && (before.getInquiryAnswerContent() == null || before.getInquiryAnswerContent().isBlank());

        InquiryDto inquiryDto = new InquiryDto();
        inquiryDto.setInquiryIdx(inquiryIdx);
        inquiryDto.setAdminIdx(adminIdx);
        inquiryDto.setInquiryAnswerContent(answerContent);
        boolean updated = inquiryMapper.answerInquiry(inquiryDto) > 0;

        // 답변 '수정'마다 메일이 가지 않도록 최초 답변 등록일 때만 발송
        if (updated && firstAnswer) {
            emailService.sendInquiryAnswered(before.getUserMail(), before.getInquiryTitle(), answerContent);
        }
        return updated;
    }
}
