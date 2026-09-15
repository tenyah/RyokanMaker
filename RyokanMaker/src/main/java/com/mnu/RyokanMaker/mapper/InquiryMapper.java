package com.mnu.RyokanMaker.mapper;

import com.mnu.RyokanMaker.dto.InquiryDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface InquiryMapper {

    /** 특정 회원이 작성한 문의 목록 (마이페이지 - 내 문의 내역) */
    List<InquiryDto> selectListByUserMail(String userMail);

    /** 문의 단건 조회 */
    InquiryDto selectByIdx(int inquiryIdx);

    /** 문의 등록 (등록 시 상태는 XML에서 '답변대기'로 고정) */
    int insert(InquiryDto inquiryDto);
}
