package com.mnu.ryokanmaker.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.mnu.ryokanmaker.dto.InquiryDto;

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

    /** status가 null이면 전체 조회 (관리자 문의 목록) */
    List<InquiryDto> selectInquiriesByAdmin(@Param("adminIdx") Integer adminIdx, @Param("status") String status);

    /** 관리자 문의 단건 조회 (본인 소유 문의만) */
    InquiryDto selectInquiry(@Param("inquiryIdx") Integer inquiryIdx, @Param("adminIdx") Integer adminIdx);

    /** 답변 등록/수정. 등록과 동시에 상태를 '답변완료'로 갱신 */
    int answerInquiry(InquiryDto inquiryDto);

    /** status가 null이면 전체 건수 */
    int countByStatus(@Param("adminIdx") Integer adminIdx, @Param("status") String status);
}
