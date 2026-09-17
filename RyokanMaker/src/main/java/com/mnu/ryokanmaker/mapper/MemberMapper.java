package com.mnu.ryokanmaker.mapper;

import com.mnu.ryokanmaker.dto.MemberDto;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MemberMapper {

    MemberDto selectByUserMail(String userMail);

    int insert(MemberDto memberDto);

    int update(MemberDto memberDto);

    /** 회원탈퇴 - 이 회원의 문의/예약을 전부 지운 뒤 마지막에 호출 (MemberService.withdraw 참고) */
    int deleteByUserMail(String userMail);
}
