package com.mnu.RyokanMaker.domain;

import lombok.Data;

@Data
public class MemberDTO {
	   private String userMail;
	    private String userPassword;
	    private String userNickname;
	    private String userAddress;
	    private String userCountry;
	    private String userTel;

	    // ---- 체크인용 실명 (회원가입 시 입력, 예약 시 그대로 불러다 씀) ----
	    private String userLastNameEn;  // Last Name (영문) - 필수
	    private String userFirstNameEn; // First Name (영문) - 필수
	    private String userLastNameJp;  // 姓 (일문) - 선택
	    private String userFirstNameJp; // 名 (일문) - 선택

}
