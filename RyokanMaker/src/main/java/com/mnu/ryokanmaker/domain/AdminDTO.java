package com.mnu.ryokanmaker.domain;

import java.util.List;

import lombok.Data;

import com.mnu.ryokanmaker.util.ImageJsonUtil;

@Data
public class AdminDTO {
    private int admin_idx;
    private String admin_id;
    private String admin_password;
    private String admin_mail;
    private String admin_name;
    private String admin_loc;
    private String ryokan_name;
    private String ryokan_tel;
    private String ryokan_access;
    private String ryokan_logo;    // RYOKAN_LOGO (CLOB) - 로고 이미지 경로 JSON 배열 문자열 저장
    private String pw_reset_yn;
    private String ryokan_image;   // RYOKAN_IMAGE (CLOB) - 메인화면 슬라이드 이미지 경로 JSON 배열 문자열 저장

    /** 로고는 1장만 쓰므로 경로 JSON 배열에서 첫 번째 경로만 뽑는다. */
    public String getLogoUrl() {
        return ImageJsonUtil.firstPath(ryokan_logo);
    }

    /** 메인 화면 슬라이드 이미지 경로 목록 (화면에서 등록된 사진을 그대로 미리보기하기 위함). */
    public List<String> getRyokanImageUrls() {
        return ImageJsonUtil.parsePaths(ryokan_image);
    }
}
