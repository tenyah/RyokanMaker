package com.mnu.ryokanmaker.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * COUNTRY_CODE 테이블 매핑 DTO (회원가입 국가/전화번호 국가코드 선택용)
 * PK : countryName
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CountryCodeDto {

    private String countryName;
    private String dialCode;
}
