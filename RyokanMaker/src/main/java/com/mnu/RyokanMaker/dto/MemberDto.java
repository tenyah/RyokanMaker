package com.mnu.RyokanMaker.dto;

/**
 * MEMBER 테이블 매핑 DTO
 * PK : userMail
 */
public class MemberDto {

    private String userMail;
    private String userPassword;
    private String userNickname;
    private String userAddress;
    private String userCountry;
    private String userTel;

    public MemberDto() {
    }

    public MemberDto(String userMail, String userPassword, String userNickname,
                      String userAddress, String userCountry, String userTel) {
        this.userMail = userMail;
        this.userPassword = userPassword;
        this.userNickname = userNickname;
        this.userAddress = userAddress;
        this.userCountry = userCountry;
        this.userTel = userTel;
    }

    public String getUserMail() { return userMail; }
    public void setUserMail(String userMail) { this.userMail = userMail; }

    public String getUserPassword() { return userPassword; }
    public void setUserPassword(String userPassword) { this.userPassword = userPassword; }

    public String getUserNickname() { return userNickname; }
    public void setUserNickname(String userNickname) { this.userNickname = userNickname; }

    public String getUserAddress() { return userAddress; }
    public void setUserAddress(String userAddress) { this.userAddress = userAddress; }

    public String getUserCountry() { return userCountry; }
    public void setUserCountry(String userCountry) { this.userCountry = userCountry; }

    public String getUserTel() { return userTel; }
    public void setUserTel(String userTel) { this.userTel = userTel; }
}
