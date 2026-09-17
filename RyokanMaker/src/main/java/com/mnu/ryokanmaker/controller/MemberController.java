package com.mnu.ryokanmaker.controller;

import com.mnu.ryokanmaker.dto.MemberDto;
import com.mnu.ryokanmaker.service.MemberService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class MemberController {

    @Autowired
    private MemberService memberService;

    @GetMapping("/member/signup")
    public String signupForm(Model model) {
        model.addAttribute("countries", memberService.listCountries());
        return "member/signup";
    }

    @PostMapping("/member/signup")
    public String signup(MemberDto memberDto,
                          @RequestParam String userCountry,
                          @RequestParam String userTelLocal,
                          @RequestParam String userPostalCode,
                          @RequestParam String userAddress1,
                          @RequestParam String userAddress2,
                          HttpSession session,
                          Model model) {

        if (memberService.existsByUserMail(memberDto.getUserMail())) {
            model.addAttribute("error", "이미 가입된 이메일입니다.");
            model.addAttribute("member", memberDto);
            model.addAttribute("countries", memberService.listCountries());
            return "member/signup";
        }

        memberDto.setUserCountry(userCountry);
        memberDto.setUserTel(memberService.dialCodeOf(userCountry) + " " + userTelLocal);
        memberDto.setUserAddress("[" + userPostalCode + "] " + userAddress1 + ", " + userAddress2);

        memberService.signup(memberDto);

        // 가입 직후 바로 로그인 상태로 만들어서, 이어서 문의 작성 등 로그인 필요한 기능을 바로 쓸 수 있게 함
        memberDto.setUserPassword(null);
        session.setAttribute("loginMember", memberDto);
        return "redirect:/";
    }

    @GetMapping("/member/login")
    public String loginForm() {
        return "member/login";
    }

    @PostMapping("/member/login")
    public String login(@RequestParam String userMail,
                         @RequestParam String userPassword,
                         HttpSession session,
                         Model model) {
        MemberDto member = memberService.authenticate(userMail, userPassword);
        if (member == null) {
            model.addAttribute("error", "이메일 또는 비밀번호가 올바르지 않습니다.");
            return "member/login";
        }
        member.setUserPassword(null);
        session.setAttribute("loginMember", member);
        return "redirect:/";
    }

    @GetMapping("/member/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}
