package com.mnu.ryokanmaker.controller;

import com.mnu.ryokanmaker.dto.MemberDto;
import com.mnu.ryokanmaker.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class MemberController {

    @Autowired
    private MemberService memberService;

    @GetMapping("/member/signup")
    public String signupForm() {
        return "member/signup";
    }

    @PostMapping("/member/signup")
    public String signup(MemberDto memberDto, Model model) {
        if (memberService.existsByUserMail(memberDto.getUserMail())) {
            model.addAttribute("error", "이미 가입된 이메일입니다.");
            model.addAttribute("member", memberDto);
            return "member/signup";
        }
        memberService.signup(memberDto);
        return "redirect:/";
    }
}
