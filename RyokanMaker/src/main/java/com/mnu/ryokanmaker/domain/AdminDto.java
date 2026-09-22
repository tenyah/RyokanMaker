package com.mnu.ryokanmaker.domain;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.mnu.ryokanmaker.util.ImageJsonUtil;

/**
 * ADMIN 테이블 매핑 DTO
 * PK : adminIdx
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminDto {

    private Integer adminIdx;
    private String adminId;
    private String adminPassword;
    private String adminName;
    private String adminMail;
    private String adminLoc;
    private String ryokanName;
    private String ryokanTel;
    private String ryokanAccess;
    private String ryokanLogo;     // RYOKAN_LOGO (CLOB) - 로고 이미지 경로 JSON 배열 문자열 저장
    private String pwResetYn;
    private String ryokanImage;    // RYOKAN_IMAGE (CLOB) - 메인화면 슬라이드 이미지 경로 JSON 배열 문자열 저장

    /** 로고는 1장만 쓰므로 경로 JSON 배열에서 첫 번째 경로만 뽑는다. */
    public String getLogoUrl() {
        return ImageJsonUtil.firstPath(ryokanLogo);
    }

    /** 메인 화면 슬라이드 이미지 경로 목록 (화면에서 등록된 사진을 그대로 미리보기하기 위함). */
    public List<String> getRyokanImageUrls() {
        return ImageJsonUtil.parsePaths(ryokanImage);
    }
}
