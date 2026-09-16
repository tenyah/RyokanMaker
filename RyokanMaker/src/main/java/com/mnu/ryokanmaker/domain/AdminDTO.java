package com.mnu.ryokanmaker.domain;

import lombok.Data;

@Data
public class AdminDTO {
    private int adminIdx;
    private String adminId;
    private String adminPassword;
    private String adminMail;
    private String adminName;
    private String adminLoc;
    private String ryokanName;
    private String ryokanTel;
    private String ryokanAccess;
    private String ryokanLogo;   
    private String pwResetYn;	
}
