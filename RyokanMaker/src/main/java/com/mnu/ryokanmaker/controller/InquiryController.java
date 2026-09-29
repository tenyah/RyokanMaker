package com.mnu.ryokanmaker.controller;

import com.mnu.ryokanmaker.config.TenantInterceptor;
import com.mnu.ryokanmaker.domain.AdminDto;
import com.mnu.ryokanmaker.domain.InquiryDto;
import com.mnu.ryokanmaker.domain.MemberDto;
import com.mnu.ryokanmaker.service.InquiryService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 유저가 로그인 후 이용하는 1:1 문의 화면 (마이페이지 성격). /r/{adminId} 밑이라 어느 료칸인지는
 * TenantInterceptor가 미리 찾아둔다.
 * - 목록(본인 문의만) : templates/inquiry/list.html
 * - 작성            : templates/inquiry/write.html
 * - 상세(본인 문의만) : templates/inquiry/view.html
 *
 * 로그인 회원 정보는 세션 속성 "loginMember:{adminId}"(MemberDto)에 들어있다고 가정합니다.
 * 같은 이메일도 료칸마다 별도 회원이라, 문의 소유권은 반드시 (adminIdx, userMail)을 같이 확인합니다.
 */
@Controller
@RequestMapping("/r/{adminId}")
public class InquiryController {

    @Autowired
    private InquiryService inquiryService;

    private MemberDto loginMember(HttpServletRequest request, HttpSession session) {
        AdminDto tenant = TenantInterceptor.currentTenant(request);
        return (MemberDto) session.getAttribute(TenantInterceptor.memberSessionKey(tenant.getAdminId()));
    }

    /** 내가 쓴 문의 목록 */
    @GetMapping("/inquiry/list")
    public String list(@PathVariable String adminId, HttpServletRequest request, HttpSession session, Model model) {
        MemberDto member = loginMember(request, session);
        if (member == null) {
            return "redirect:/r/" + adminId + "/member/login";
        }
        AdminDto tenant = TenantInterceptor.currentTenant(request);
        model.addAttribute("inquiryList", inquiryService.listByMember(tenant.getAdminIdx(), member.getUserMail()));
        return "inquiry/list";
    }

    /** 문의 작성 폼 */
    @GetMapping("/inquiry/write")
    public String writeForm(@PathVariable String adminId, HttpServletRequest request, HttpSession session, Model model) {
        if (loginMember(request, session) == null) {
            return "redirect:/r/" + adminId + "/member/login";
        }
        AdminDto tenant = TenantInterceptor.currentTenant(request);
        model.addAttribute("adminIdx", tenant.getAdminIdx());
        return "inquiry/write";
    }

    /** 문의 등록 처리 */
    @PostMapping("/inquiry/write")
    public String write(@PathVariable String adminId, HttpServletRequest request, HttpSession session, InquiryDto inquiryDto) {
        MemberDto member = loginMember(request, session);
        if (member == null) {
            return "redirect:/r/" + adminId + "/member/login";
        }
        inquiryDto.setUserMail(member.getUserMail());
        inquiryService.write(inquiryDto);
        return "redirect:/r/" + adminId + "/inquiry/list";
    }

    /** 문의 상세 (본인 문의만 조회 가능) */
    @GetMapping("/inquiry/view")
    public String view(@PathVariable String adminId, HttpServletRequest request, HttpSession session,
            @RequestParam int idx, Model model) {
        MemberDto member = loginMember(request, session);
        if (member == null) {
            return "redirect:/r/" + adminId + "/member/login";
        }
        AdminDto tenant = TenantInterceptor.currentTenant(request);
        InquiryDto inquiry = inquiryService.select(idx);
        if (inquiry == null || !inquiry.getUserMail().equals(member.getUserMail())
                || !inquiry.getAdminIdx().equals(tenant.getAdminIdx())) {
            return "redirect:/r/" + adminId + "/inquiry/list";
        }
        model.addAttribute("inquiry", inquiry);
        return "inquiry/view";
    }

    /** 문의 삭제 (본인 글만) */
    @PostMapping("/inquiry/delete")
    public String delete(@PathVariable String adminId, HttpServletRequest request, HttpSession session, @RequestParam int idx) {
        MemberDto member = loginMember(request, session);
        if (member == null) {
            return "redirect:/r/" + adminId + "/member/login";
        }
        AdminDto tenant = TenantInterceptor.currentTenant(request);
        inquiryService.delete(idx, tenant.getAdminIdx(), member.getUserMail());
        return "redirect:/r/" + adminId + "/inquiry/list";
    }
}
