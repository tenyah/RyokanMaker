package com.mnu.ryokanmaker.service;

import com.mnu.ryokanmaker.dto.AdminReservationListItemDto;
import com.mnu.ryokanmaker.dto.CountryCodeDto;
import com.mnu.ryokanmaker.dto.MemberDto;
import com.mnu.ryokanmaker.mapper.CountryCodeMapper;
import com.mnu.ryokanmaker.mapper.InquiryMapper;
import com.mnu.ryokanmaker.mapper.MemberMapper;
import com.mnu.ryokanmaker.mapper.ReservationMapper;
import com.mnu.ryokanmaker.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MemberService {

    @Autowired
    private MemberMapper memberMapper;

    @Autowired
    private CountryCodeMapper countryCodeMapper;

    @Autowired
    private ReservationMapper reservationMapper;

    @Autowired
    private InquiryMapper inquiryMapper;

    /** 마이페이지 - 내 예약 현황 */
    public List<AdminReservationListItemDto> getReservationHistory(String userMail) {
        return reservationMapper.selectReservationListByUserMail(userMail);
    }

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

    /** 로그인 : 이메일/비밀번호가 맞으면 회원 정보를, 아니면 null을 반환 */
    public MemberDto authenticate(String userMail, String userPassword) {
        MemberDto member = memberMapper.selectByUserMail(userMail);
        if (member == null) {
            return null;
        }
        if (!member.getUserPassword().equals(PasswordUtil.sha256(userPassword))) {
            return null;
        }
        return member;
    }

    public MemberDto findByUserMail(String userMail) {
        return memberMapper.selectByUserMail(userMail);
    }

    /**
     * 마이페이지 정보 수정. newPassword가 비어있으면 기존 비밀번호를 그대로 유지.
     * memberDto에는 userMail만 채워져 있어도 되고, 나머지는 이 메서드가 DB에서 채운 뒤 덮어씀.
     */
    public MemberDto updateProfile(MemberDto memberDto, String newPassword) {
        String passwordToSave;
        if (newPassword == null || newPassword.isBlank()) {
            MemberDto current = memberMapper.selectByUserMail(memberDto.getUserMail());
            passwordToSave = current.getUserPassword();
        } else {
            passwordToSave = PasswordUtil.sha256(newPassword);
        }
        memberDto.setUserPassword(passwordToSave);
        memberMapper.update(memberDto);
        return memberDto;
    }

    /**
     * 회원탈퇴 - 이 회원과 관련된 문의/예약을 전부 지운 뒤 회원 정보를 삭제한다.
     * FK 순서: ROOM/ONSEN/RESTAURANT_RESERVATION -> RESERVATION -> INQUIRY -> MEMBER
     * 도중에 하나라도 실패하면 전체 롤백되도록 트랜잭션으로 묶는다.
     */
    @Transactional
    public void withdraw(String userMail) {
        reservationMapper.deleteRoomReservationsByUserMail(userMail);
        reservationMapper.deleteOnsenReservationsByUserMail(userMail);
        reservationMapper.deleteRestaurantReservationsByUserMail(userMail);
        reservationMapper.deleteReservationsByUserMail(userMail);
        inquiryMapper.deleteByUserMail(userMail);
        memberMapper.deleteByUserMail(userMail);
    }
}
