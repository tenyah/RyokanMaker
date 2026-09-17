package com.mnu.ryokanmaker.controller;

import com.mnu.ryokanmaker.dto.MemberDto;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * 최신 Thymeleaf(Spring 6)에서는 템플릿에서 #session을 직접 쓸 수 없어서,
 * 모든 컨트롤러의 응답에 로그인 회원 정보를 자동으로 모델에 넣어준다.
 * (헤더 프래그먼트가 로그인 상태를 보여주는 데 사용 - templates/include/header.html)
 */
@ControllerAdvice
public class GlobalModelAdvice {

    @ModelAttribute("loginMember")
    public MemberDto loginMember(HttpSession session) {
        return (MemberDto) session.getAttribute("loginMember");
    }
}
