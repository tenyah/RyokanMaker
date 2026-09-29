package com.mnu.ryokanmaker.controller;

import com.mnu.ryokanmaker.config.TenantInterceptor;
import com.mnu.ryokanmaker.domain.AdminDto;
import com.mnu.ryokanmaker.domain.MemberDto;
import com.mnu.ryokanmaker.service.MemberService;
import com.mnu.ryokanmaker.util.NameValidationUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 회원가입/로그인/마이페이지/탈퇴. /r/{adminId} 밑이라 어느 료칸인지는 TenantInterceptor가 미리 찾아둔다.
 * 같은 이메일도 료칸마다 별도 회원(MEMBER의 PK가 adminIdx+userMail)이라, 모든 조회/저장에 adminIdx를 같이 쓴다.
 * 로그인 세션도 "loginMember:{adminId}"로 분리해서, 한 브라우저에서 여러 료칸에 동시에 로그인해도 서로 안 섞인다.
 */
@Controller
@RequestMapping("/r/{adminId}")
public class MemberController {

    private static final Logger log = LoggerFactory.getLogger(MemberController.class);

    @Autowired
    private MemberService memberService;

    private AdminDto tenant(HttpServletRequest request) {
        return TenantInterceptor.currentTenant(request);
    }

    @GetMapping("/member/signup")
    public String signupForm(Model model) {
        model.addAttribute("countries", memberService.listCountries());
        return "member/signup";
    }

    @PostMapping("/member/signup")
    public String signup(HttpServletRequest request,
                          MemberDto memberDto,
                          @RequestParam String userCountry,
                          @RequestParam String userTelLocal,
                          @RequestParam String userPostalCode,
                          @RequestParam String userAddress1,
                          @RequestParam String userAddress2,
                          Model model) {

        AdminDto tenant = tenant(request);
        memberDto.setAdminIdx(tenant.getAdminIdx());

        if (memberService.existsByUserMail(tenant.getAdminIdx(), memberDto.getUserMail())) {
            model.addAttribute("error", "error.member.duplicate_email");
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
        return "redirect:/r/" + tenant.getAdminId() + "/member/login?signup=success";
    }

    @GetMapping("/member/login")
    public String loginForm() {
        return "member/login";
    }

    @PostMapping("/member/login")
    public String login(HttpServletRequest request,
                         @RequestParam String userMail,
                         @RequestParam String userPassword,
                         HttpSession session,
                         Model model) {
        AdminDto tenant = tenant(request);
        MemberDto member = memberService.authenticate(tenant.getAdminIdx(), userMail, userPassword);
        if (member == null) {
            model.addAttribute("error", "error.member.login_failed");
            return "member/login";
        }
        member.setUserPassword(null);
        session.setAttribute(TenantInterceptor.memberSessionKey(tenant.getAdminId()), member);
        return "redirect:/r/" + tenant.getAdminId();
    }

    @GetMapping("/member/forgot")
    public String forgotForm() {
        return "member/forgot";
    }

    /**
     * 비밀번호 찾기 : 가입된 이메일이면 임시 비밀번호를 메일로 보낸다.
     * 가입 여부와 관계없이 같은 안내를 보여줘서 이메일이 가입돼 있는지 밖에서 알 수 없게 한다.
     */
    @PostMapping("/member/forgot")
    public String forgot(HttpServletRequest request, @RequestParam String userMail, Model model) {
        if (userMail == null || userMail.isBlank()) {
            model.addAttribute("error", "error.member.forgot_empty");
            return "member/forgot";
        }
        if (!memberService.isMailAvailable()) {
            model.addAttribute("error", "error.member.forgot_mail_unavailable");
            return "member/forgot";
        }
        try {
            memberService.sendTempPassword(tenant(request).getAdminIdx(), userMail);
        } catch (Exception e) {
            // 메일 서버 오류 등: 비밀번호는 바뀌지 않았고, 이메일 가입 여부가 드러나지 않도록 같은 안내를 보여준다
            log.warn("임시 비밀번호 메일 발송 실패", e);
        }
        model.addAttribute("sent", true);
        return "member/forgot";
    }

    /** 이 료칸에서만 로그아웃 (같은 세션의 다른 료칸 로그인은 유지됨). */
    @GetMapping("/member/logout")
    public String logout(HttpServletRequest request, HttpSession session) {
        AdminDto tenant = tenant(request);
        session.removeAttribute(TenantInterceptor.memberSessionKey(tenant.getAdminId()));
        return "redirect:/r/" + tenant.getAdminId();
    }

    /** 회원탈퇴 - 내 문의/예약을 전부 지운 뒤 회원 정보를 삭제하고 이 료칸에서만 로그아웃 처리 */
    @PostMapping("/member/withdraw")
    public String withdraw(HttpServletRequest request, HttpSession session) {
        AdminDto tenant = tenant(request);
        MemberDto loginMember = (MemberDto) session.getAttribute(TenantInterceptor.memberSessionKey(tenant.getAdminId()));
        if (loginMember == null) {
            return "redirect:/r/" + tenant.getAdminId() + "/member/login";
        }
        memberService.withdraw(tenant.getAdminIdx(), loginMember.getUserMail());
        session.removeAttribute(TenantInterceptor.memberSessionKey(tenant.getAdminId()));
        return "redirect:/r/" + tenant.getAdminId() + "?withdraw=success";
    }

    @PostMapping("/member/reservation_cancel")
    public String reservationCancel(HttpServletRequest request, @RequestParam("resvNum") Integer resvNum,
                                     HttpSession session, RedirectAttributes redirectAttributes) {
        AdminDto tenant = tenant(request);
        MemberDto loginMember = (MemberDto) session.getAttribute(TenantInterceptor.memberSessionKey(tenant.getAdminId()));
        if (loginMember == null) {
            return "redirect:/r/" + tenant.getAdminId() + "/member/login";
        }
        try {
            memberService.cancelReservation(tenant.getAdminIdx(), resvNum, loginMember.getUserMail());
            redirectAttributes.addFlashAttribute("message", "mypage.resv_cancel_done");
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.warn("마이페이지 예약 취소 실패 resvNum={}", resvNum, e);
            redirectAttributes.addFlashAttribute("error", "mypage.resv_cancel_fail");
        }
        return "redirect:/r/" + tenant.getAdminId() + "/member/mypage";
    }

    @GetMapping("/member/mypage")
    public String mypageForm(HttpServletRequest request, HttpSession session, Model model) {
        AdminDto tenant = tenant(request);
        MemberDto loginMember = (MemberDto) session.getAttribute(TenantInterceptor.memberSessionKey(tenant.getAdminId()));
        if (loginMember == null) {
            return "redirect:/r/" + tenant.getAdminId() + "/member/login";
        }
        model.addAttribute("member", memberService.findByUserMail(tenant.getAdminIdx(), loginMember.getUserMail()));
        model.addAttribute("countries", memberService.listCountries());
        model.addAttribute("reservations", memberService.getReservationHistory(tenant.getAdminIdx(), loginMember.getUserMail()));
        return "member/mypage";
    }

    @PostMapping("/member/mypage")
    public String mypageUpdate(HttpServletRequest request,
                                HttpSession session,
                                MemberDto memberDto,
                                @RequestParam(required = false) String newPassword,
                                Model model) {
        AdminDto tenant = tenant(request);
        MemberDto loginMember = (MemberDto) session.getAttribute(TenantInterceptor.memberSessionKey(tenant.getAdminId()));
        if (loginMember == null) {
            return "redirect:/r/" + tenant.getAdminId() + "/member/login";
        }
        // 세션의 (료칸, 이메일)을 그대로 쓰고, 폼에서 온 값은 무시 (본인 계정만 수정 가능하게)
        memberDto.setAdminIdx(tenant.getAdminIdx());
        memberDto.setUserMail(loginMember.getUserMail());

        String nameError = validateNames(memberDto);
        if (nameError != null) {
            model.addAttribute("member", memberDto);
            model.addAttribute("countries", memberService.listCountries());
            model.addAttribute("reservations", memberService.getReservationHistory(tenant.getAdminIdx(), loginMember.getUserMail()));
            model.addAttribute("error", nameError);
            return "member/mypage";
        }

        MemberDto updated = memberService.updateProfile(memberDto, newPassword);
        updated.setUserPassword(null);
        session.setAttribute(TenantInterceptor.memberSessionKey(tenant.getAdminId()), updated);

        model.addAttribute("member", updated);
        model.addAttribute("countries", memberService.listCountries());
        model.addAttribute("reservations", memberService.getReservationHistory(tenant.getAdminIdx(), updated.getUserMail()));
        model.addAttribute("message", "member.updated");
        return "member/mypage";
    }

    /** 영문 이름은 알파벳만, 일본어 이름은 히라가나/가타카나만(한자 불가) 허용. 문제 없으면 null 반환. */
    private String validateNames(MemberDto memberDto) {
        if (!NameValidationUtil.isValidEnglishName(memberDto.getUserLastNameEn())
                || !NameValidationUtil.isValidEnglishName(memberDto.getUserFirstNameEn())) {
            return "error.member.name_en";
        }
        if (!NameValidationUtil.isValidJapaneseNameOrBlank(memberDto.getUserLastNameJp())
                || !NameValidationUtil.isValidJapaneseNameOrBlank(memberDto.getUserFirstNameJp())) {
            return "error.member.name_jp";
        }
        return null;
    }
}
