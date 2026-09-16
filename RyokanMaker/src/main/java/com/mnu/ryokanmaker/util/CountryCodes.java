package com.mnu.ryokanmaker.util;

import java.util.LinkedHashMap;
import java.util.Map;

/** 회원가입 국가 선택 + 전화번호 국가코드 반영용 (국가명 -> 국제전화 코드) */
public class CountryCodes {

    public static final Map<String, String> DIAL_CODES = new LinkedHashMap<>();
    static {
        DIAL_CODES.put("대한민국", "+82");
        DIAL_CODES.put("일본", "+81");
        DIAL_CODES.put("중국", "+86");
        DIAL_CODES.put("대만", "+886");
        DIAL_CODES.put("홍콩", "+852");
        DIAL_CODES.put("싱가포르", "+65");
        DIAL_CODES.put("태국", "+66");
        DIAL_CODES.put("베트남", "+84");
        DIAL_CODES.put("필리핀", "+63");
        DIAL_CODES.put("미국", "+1");
        DIAL_CODES.put("캐나다", "+1");
        DIAL_CODES.put("영국", "+44");
        DIAL_CODES.put("프랑스", "+33");
        DIAL_CODES.put("독일", "+49");
        DIAL_CODES.put("호주", "+61");
    }

    public static String dialCodeOf(String countryName) {
        return DIAL_CODES.getOrDefault(countryName, "");
    }
}
