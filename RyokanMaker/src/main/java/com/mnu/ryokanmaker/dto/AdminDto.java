package com.mnu.ryokanmaker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private String ryokanFacility;
    private String ryokanTel;
    private String ryokanAccess;
    private String ryokanLogo;
    private String pwResetYn;
}
