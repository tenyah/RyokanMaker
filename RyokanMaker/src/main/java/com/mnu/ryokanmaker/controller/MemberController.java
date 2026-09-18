package com.mnu.ryokanmaker.controller;

import com.mnu.ryokanmaker.dto.MemberDto;
import com.mnu.ryokanmaker.service.MemberService;
import com.mnu.ryokanmaker.util.NameValidationUtil;
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
                          Model model) {

        if (memberService.existsByUserMail(memberDto.getUserMail())) {
            model.addAttribute("error", "이미 가입된 이메일입니다.");
            model.addAttribute("member", memberDto);
            model.addAttribute("countries", memberService.listCountries());
            return "member/signup";
        }

        String nameError = validateNames(memberDto);
        if (nameError != null) {
            model.addAttribute("error", nameError);
            model.addAttribute("member", memberDto);
            model.addAttribute("countries", memberService.listCountries());
            return "member/signup";
        }

        memberDto.setUserCountry(userCountry);
        memberDto.setUserTel(memberService.dialCodeOf(userCountry) + " " + userTelLocal);
        memberDto.setUserAddress("[" + userPostalCode + "] " + userAddress1 + ", " + userAddress2);

        memberService.signup(memberDto);

        // 가입만 처리하고 자동 로그인은 시키지 않음 - 회원이 직접 로그인하도록 안내
        return "redirect:/member/login?signup=success";
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

    /** 회원탈퇴 - 내 문의/예약을 전부 지운 뒤 회원 정보를 삭제하고 로그아웃 처리 */
    @PostMapping("/member/withdraw")
    public String withdraw(HttpSession session) {
        MemberDto loginMember = (MemberDto) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/member/login";
        }
        memberService.withdraw(loginMember.getUserMail());
        session.invalidate();
        return "redirect:/?withdraw=success";
    }

    @GetMapping("/member/mypage")
    public String mypageForm(HttpSession session, Model model) {
        MemberDto loginMember = (MemberDto) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/member/login";
        }
        model.addAttribute("member", memberService.findByUserMail(loginMember.getUserMail()));
        model.addAttribute("countries", memberService.listCountries());
        model.addAttribute("reservations", memberService.getReservationHistory(loginMember.getUserMail()));
        return "member/mypage";
    }

    @PostMapping("/member/mypage")
    public String mypageUpdate(HttpSession session,
                                MemberDto memberDto,
                                @RequestParam(required = false) String newPassword,
                                Model model) {
        MemberDto loginMember = (MemberDto) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/member/login";
        }
        // 세션의 이메일을 그대로 쓰고, 폼에서 온 이메일은 무시 (본인 계정만 수정 가능하게)
        memberDto.setUserMail(loginMember.getUserMail());

        String nameError = validateNames(memberDto);
        if (nameError != null) {
            model.addAttribute("member", memberDto);
            model.addAttribute("countries", memberService.listCountries());
            model.addAttribute("reservations", memberService.getReservationHistory(loginMember.getUserMail()));
            model.addAttribute("error", nameError);
            return "member/mypage";
        }

        MemberDto updated = memberService.updateProfile(memberDto, newPassword);
        updated.setUserPassword(null);
        session.setAttribute("loginMember", updated);

        model.addAttribute("member", updated);
        model.addAttribute("countries", memberService.listCountries());
        model.addAttribute("reservations", memberService.getReservationHistory(updated.getUserMail()));
        model.addAttribute("message", "정보가 수정되었습니다.");
        return "member/mypage";
    }

    /** 영문 이름은 알파벳만, 일본어 이름은 히라가나/가타카나만(한자 불가) 허용. 문제 없으면 null 반환. */
    private String validateNames(MemberDto memberDto) {
        if (!NameValidationUtil.isValidEnglishName(memberDto.getUserLastNameEn())
                || !NameValidationUtil.isValidEnglishName(memberDto.getUserFirstNameEn())) {
            return "영문 이름은 알파벳으로만 입력해주세요.";
        }
        if (!NameValidationUtil.isValidJapaneseNameOrBlank(memberDto.getUserLastNameJp())
                || !NameValidationUtil.isValidJapaneseNameOrBlank(memberDto.getUserFirstNameJp())) {
            return "일본어 이름은 히라가나/가타카나로만 입력해주세요 (한자 불가).";
        }
        return null;
    }
}
