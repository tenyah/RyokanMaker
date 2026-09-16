package com.mnu.ryokanmaker.service;

import com.mnu.ryokanmaker.dto.MemberDto;
import com.mnu.ryokanmaker.mapper.MemberMapper;
import com.mnu.ryokanmaker.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MemberService {

    @Autowired
    private MemberMapper memberMapper;

    public boolean existsByUserMail(String userMail) {
        return memberMapper.selectByUserMail(userMail) != null;
    }

    public void signup(MemberDto memberDto) {
        memberDto.setUserPassword(PasswordUtil.sha256(memberDto.getUserPassword()));
        memberMapper.insert(memberDto);
    }
}
