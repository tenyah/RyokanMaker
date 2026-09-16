package com.mnu.ryokanmaker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MEMBER 테이블 매핑 DTO
 * PK : userMail
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberDto {

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
