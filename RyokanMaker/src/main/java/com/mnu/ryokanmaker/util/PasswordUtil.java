package com.mnu.ryokanmaker.util;

import java.security.MessageDigest;
import java.security.SecureRandom;

public class PasswordUtil {

    private static final String RANDOM_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";

    /** SHA-256 해시 (기존 exSampleThymeleaf UserSHA256와 동일한 방식) */
    public static String sha256(String str) {
        StringBuilder sbuf = new StringBuilder();
        try {
            MessageDigest mDigest = MessageDigest.getInstance("SHA-256");
            mDigest.update(str.getBytes());
            byte[] msgStr = mDigest.digest();
            for (byte b : msgStr) {
                String tmpEncTxt = Integer.toString((b & 0xff) + 0x100, 16).substring(1);
                sbuf.append(tmpEncTxt);
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        return sbuf.toString();
    }

    /** 관리자 계정 승인 시 발급할 임시 비밀번호 (평문, 이메일 발송 후 해시해서 저장) */
    public static String generateTempPassword() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            sb.append(RANDOM_CHARS.charAt(random.nextInt(RANDOM_CHARS.length())));
        }
        return sb.toString();
    }
}
