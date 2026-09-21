package com.mnu.ryokanmaker.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * DB에 보관하는 비밀값(관리자 메일 앱 비밀번호)을 AES-256-GCM으로 암호화/복호화한다.
 * 키는 환경변수 MAIL_SECRET_KEY(아무 긴 문자열)에서 SHA-256으로 만들며, 키가 없으면 사용할 수 없다.
 * 저장 형식: Base64( IV 12바이트 + 암호문 )
 */
@Component
public class SecretCipher {

    private static final int IV_LENGTH = 12;
    private static final int TAG_BITS = 128;

    private final SecureRandom random = new SecureRandom();
    private final byte[] keyBytes;

    public SecretCipher(@Value("${mail.secret.key:}") String secret) {
        this.keyBytes = (secret == null || secret.isBlank()) ? null : sha256(secret);
    }

    public boolean isAvailable() {
        return keyBytes != null;
    }

    public String encrypt(String plain) {
        requireKey();
        try {
            byte[] iv = new byte[IV_LENGTH];
            random.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(keyBytes, "AES"), new GCMParameterSpec(TAG_BITS, iv));
            byte[] enc = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));
            byte[] out = new byte[iv.length + enc.length];
            System.arraycopy(iv, 0, out, 0, iv.length);
            System.arraycopy(enc, 0, out, iv.length, enc.length);
            return Base64.getEncoder().encodeToString(out);
        } catch (Exception e) {
            throw new IllegalStateException("암호화에 실패했습니다.", e);
        }
    }

    /** 복호화. 키가 다르거나 값이 손상됐으면 예외. */
    public String decrypt(String stored) {
        requireKey();
        try {
            byte[] in = Base64.getDecoder().decode(stored);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(keyBytes, "AES"),
                    new GCMParameterSpec(TAG_BITS, in, 0, IV_LENGTH));
            byte[] plain = cipher.doFinal(in, IV_LENGTH, in.length - IV_LENGTH);
            return new String(plain, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("복호화에 실패했습니다. MAIL_SECRET_KEY가 바뀌었는지 확인하세요.", e);
        }
    }

    private void requireKey() {
        if (keyBytes == null) {
            throw new IllegalStateException("MAIL_SECRET_KEY 환경변수가 설정되어 있지 않습니다.");
        }
    }

    private static byte[] sha256(String s) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
