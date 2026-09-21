package com.mnu.ryokanmaker.util;

import java.util.regex.Pattern;

/** 회원가입/마이페이지의 영문 이름·일본어 이름 입력값 검증. */
public class NameValidationUtil {

    /** 영문 이름 : 알파벳, 공백, 아포스트로피(') , 하이픈(-)만 허용 */
    private static final Pattern ENGLISH_NAME = Pattern.compile("^[A-Za-z][A-Za-z '-]*$");

    /** 일본어 이름 : 히라가나 + 가타카나 (+ 장음부호 ー, 나카구로 ・)만 허용, 한자(漢字) 불가. 선택 항목이라 빈 값은 통과. */
    private static final Pattern JAPANESE_NAME = Pattern.compile("^[\\u3040-\\u309F\\u30A0-\\u30FF]*$");

    public static boolean isValidEnglishName(String name) {
        return name != null && ENGLISH_NAME.matcher(name).matches();
    }

    /** 일본어 이름은 선택 입력이라 null/빈 문자열도 유효로 취급 */
    public static boolean isValidJapaneseNameOrBlank(String name) {
        return name == null || name.isBlank() || JAPANESE_NAME.matcher(name).matches();
    }
}
