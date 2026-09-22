package com.mnu.ryokanmaker.controller;

import com.mnu.ryokanmaker.domain.InquiryDto;
import com.mnu.ryokanmaker.domain.MemberDto;
import com.mnu.ryokanmaker.service.InquiryService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 유저가 로그인 후 이용하는 1:1 문의 화면 (마이페이지 성격).
 * - 목록(본인 문의만) : templates/inquiry/list.html
 * - 작성            : templates/inquiry/write.html
 * - 상세(본인 문의만) : templates/inquiry/view.html
 *
 * 로그인 회원 정보는 세션 속성 "loginMember"(MemberDto)에 들어있다고 가정합니다.
 * 로그인 화면/필터가 아직 없다면, 로그인 처리 시 session.setAttribute("loginMember", member) 만 맞춰주면 바로 동작합니다.
 *
 * adminIdx(문의 대상 료칸) : 현재는 사이트 자체가 단일 료칸(清流庵) 기준이라 기본값 1을 사용합니다.
 * 추후 여러 료칸을 다루게 되면 쿼리 파라미터로 넘겨 받으면 됩니다.
 */
@Controller
public class InquiryController {

    private static final int DEFAULT_ADMIN_IDX = 1;

    @Autowired
    private InquiryService inquiryService;

    private MemberDto loginMember(HttpSession session) {
        return (MemberDto) session.getAttribute("loginMember");
    }

    /** 내가 쓴 문의 목록 */
    @GetMapping("/inquiry/list")
    public String list(HttpSession session, Model model) {
        MemberDto member = loginMember(session);
        if (member == null) {
            return "redirect:/member/login";
        }
        model.addAttribute("inquiryList", inquiryService.listByMember(member.getUserMail()));
        return "inquiry/list";
    }

    /** 문의 작성 폼 */
    @GetMapping("/inquiry/write")
    public String writeForm(HttpSession session, @RequestParam(defaultValue = "" + DEFAULT_ADMIN_IDX) int adminIdx, Model model) {
        if (loginMember(session) == null) {
            return "redirect:/member/login";
        }
        model.addAttribute("adminIdx", adminIdx);
        return "inquiry/write";
    }

    /** 문의 등록 처리 */
    @PostMapping("/inquiry/write")
    public String write(HttpSession session, InquiryDto inquiryDto) {
        MemberDto member = loginMember(session);
        if (member == null) {
            return "redirect:/member/login";
        }
        inquiryDto.setUserMail(member.getUserMail());
        inquiryService.write(inquiryDto);
        return "redirect:/inquiry/list";
    }

    /** 문의 상세 (본인 문의만 조회 가능) */
    @GetMapping("/inquiry/view")
    public String view(HttpSession session, @RequestParam int idx, Model model) {
        MemberDto member = loginMember(session);
        if (member == null) {
            return "redirect:/member/login";
        }
        InquiryDto inquiry = inquiryService.select(idx);
        if (inquiry == null || !inquiry.getUserMail().equals(member.getUserMail())) {
            return "redirect:/inquiry/list";
        }
        model.addAttribute("inquiry", inquiry);
        return "inquiry/view";
    }

    /** 문의 삭제 (본인 글만) */
    @PostMapping("/inquiry/delete")
    public String delete(HttpSession session, @RequestParam int idx) {
        MemberDto member = loginMember(session);
        if (member == null) {
            return "redirect:/member/login";
        }
        inquiryService.delete(idx, member.getUserMail());
        return "redirect:/inquiry/list";
    }
}
