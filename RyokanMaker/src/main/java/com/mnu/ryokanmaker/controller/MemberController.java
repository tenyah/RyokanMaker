package com.mnu.ryokanmaker.controller;

import com.mnu.ryokanmaker.dto.MemberDto;
import com.mnu.ryokanmaker.service.MemberService;
import com.mnu.ryokanmaker.util.CountryCodes;
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
        model.addAttribute("countries", CountryCodes.DIAL_CODES.keySet());
        return "member/signup";
    }

    @PostMapping("/member/signup")
    public String signup(MemberDto memberDto,
                          @RequestParam String userCountry,
                          @RequestParam String userTelLocal,
                          @RequestParam String userPostalCode,
                          @RequestParam String userAddress1,
                          @RequestParam String userAddress2,
                          Model model) {

        if (memberService.existsByUserMail(memberDto.getUserMail())) {
            model.addAttribute("error", "이미 가입된 이메일입니다.");
            model.addAttribute("member", memberDto);
            model.addAttribute("countries", CountryCodes.DIAL_CODES.keySet());
            return "member/signup";
        }

        memberDto.setUserCountry(userCountry);
        memberDto.setUserTel(CountryCodes.dialCodeOf(userCountry) + " " + userTelLocal);
        memberDto.setUserAddress("[" + userPostalCode + "] " + userAddress1 + ", " + userAddress2);

        memberService.signup(memberDto);
        return "redirect:/";
    }
}
