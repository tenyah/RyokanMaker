package com.mnu.ryokanmaker.service;

import com.mnu.ryokanmaker.dto.CountryCodeDto;
import com.mnu.ryokanmaker.dto.MemberDto;
import com.mnu.ryokanmaker.mapper.CountryCodeMapper;
import com.mnu.ryokanmaker.mapper.MemberMapper;
import com.mnu.ryokanmaker.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemberService {

    @Autowired
    private MemberMapper memberMapper;

    @Autowired
    private CountryCodeMapper countryCodeMapper;

    public boolean existsByUserMail(String userMail) {
        return memberMapper.selectByUserMail(userMail) != null;
    }

    /** 회원가입 폼의 국가 선택 드롭다운 채우기용 (COUNTRY_CODE 테이블) */
    public List<CountryCodeDto> listCountries() {
        return countryCodeMapper.selectAll();
    }

    public String dialCodeOf(String countryName) {
        return countryCodeMapper.selectDialCode(countryName);
    }

    public void signup(MemberDto memberDto) {
        memberDto.setUserPassword(PasswordUtil.sha256(memberDto.getUserPassword()));
        memberMapper.insert(memberDto);
    }
}
