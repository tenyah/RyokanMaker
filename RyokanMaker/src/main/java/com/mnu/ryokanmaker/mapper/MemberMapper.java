package com.mnu.ryokanmaker.mapper;

import com.mnu.ryokanmaker.dto.MemberDto;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MemberMapper {

    MemberDto selectByUserMail(String userMail);

    int insert(MemberDto memberDto);

    int update(MemberDto memberDto);
}
