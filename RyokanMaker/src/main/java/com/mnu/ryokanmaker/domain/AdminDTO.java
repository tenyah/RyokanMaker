package com.mnu.ryokanmaker.domain;

import lombok.Data;

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
    private String ryokan_logo;
    private String pw_reset_yn;
    private String ryokan_image;   // RYOKAN_IMAGE (CLOB) - 메인화면 슬라이드 이미지 JSON 배열(base64) 저장
}
