package com.mnu.ryokanmaker.dto;

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
    private String userLastNameEn;
    private String userFirstNameEn;
    private String userLastNameJp;
    private String userFirstNameJp;

    public MemberDto() {
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

    public String getUserLastNameEn() { return userLastNameEn; }
    public void setUserLastNameEn(String userLastNameEn) { this.userLastNameEn = userLastNameEn; }

    public String getUserFirstNameEn() { return userFirstNameEn; }
    public void setUserFirstNameEn(String userFirstNameEn) { this.userFirstNameEn = userFirstNameEn; }

    public String getUserLastNameJp() { return userLastNameJp; }
    public void setUserLastNameJp(String userLastNameJp) { this.userLastNameJp = userLastNameJp; }

    public String getUserFirstNameJp() { return userFirstNameJp; }
    public void setUserFirstNameJp(String userFirstNameJp) { this.userFirstNameJp = userFirstNameJp; }
}
