package com.mnu.ryokanmaker.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MEMBER 테이블 매핑 DTO
 * PK : (adminIdx, userMail) - 같은 이메일도 료칸(관리자)마다 별도 회원으로 가입 가능
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberDto {

    private Integer adminIdx;
    private String userMail;
    private String userPassword;
    private String userNickname;
    private String userAddress;
    private String userCountry;
    private String userTel;
    private String userLastNameEn;
    private String userFirstNameEn;
    private String userLastNameJp;
    private String userFirstNameJp;
}
