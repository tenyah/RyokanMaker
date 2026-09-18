package com.mnu.ryokanmaker.dto;

/**
 * COUNTRY_CODE 테이블 매핑 DTO (회원가입 국가/전화번호 국가코드 선택용)
 * PK : countryName
 */
public class CountryCodeDto {

    private String countryName;
    private String dialCode;

    public CountryCodeDto() {
    }

    public CountryCodeDto(String countryName, String dialCode) {
        this.countryName = countryName;
        this.dialCode = dialCode;
    }

    public String getCountryName() { return countryName; }
    public void setCountryName(String countryName) { this.countryName = countryName; }

    public String getDialCode() { return dialCode; }
    public void setDialCode(String dialCode) { this.dialCode = dialCode; }
}
