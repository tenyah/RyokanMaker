package com.mnu.ryokanmaker.mapper;

import com.mnu.ryokanmaker.domain.CountryCodeDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CountryCodeMapper {

    /** 국가명 가나다순 전체 목록 (회원가입 국가 선택 드롭다운용) */
    List<CountryCodeDto> selectAll();

    /** 국가명으로 국제전화 코드 조회 */
    String selectDialCode(String countryName);
}
