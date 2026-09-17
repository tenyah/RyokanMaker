package com.mnu.ryokanmaker.mapper;

import com.mnu.ryokanmaker.dto.InquiryDto;
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

    /** 문의 1건 삭제 (본인 소유 확인은 Service에서 먼저 처리) */
    int deleteByIdx(int inquiryIdx);

    /** 회원탈퇴 시 해당 회원이 쓴 문의 전체 삭제 */
    int deleteByUserMail(String userMail);
}
