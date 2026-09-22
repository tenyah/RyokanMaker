package com.mnu.ryokanmaker.mapper;

import com.mnu.ryokanmaker.dto.MemberDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MemberMapper {

    MemberDto selectByUserMail(String userMail);

    int insert(MemberDto memberDto);

    int update(MemberDto memberDto);

    /** 비밀번호만 변경 (비밀번호 찾기 - 임시 비밀번호 반영용). 값은 이미 해시된 상태여야 한다. */
    int updatePassword(@Param("userMail") String userMail, @Param("userPassword") String userPassword);

    /** 회원탈퇴 - 이 회원의 문의/예약을 전부 지운 뒤 마지막에 호출 (MemberService.withdraw 참고) */
    int deleteByUserMail(String userMail);
}
