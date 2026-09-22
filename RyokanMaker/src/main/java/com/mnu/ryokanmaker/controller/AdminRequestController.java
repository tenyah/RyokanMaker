package com.mnu.ryokanmaker.controller;

import com.mnu.ryokanmaker.domain.AdminRequestDto;
import com.mnu.ryokanmaker.service.AdminRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 관리자 계정 신청 (공개 폼) + 신청 승인/반려 (관리자 전용).
 */
@Controller
public class AdminRequestController {

    @Autowired
    private AdminRequestService adminRequestService;

    /** 관리자 계정 신청 폼 (공개) */
    @GetMapping("/admin/apply")
    public String applyForm() {
        return "admin/admin_apply";
    }

    /** 관리자 계정 신청 등록 */
    @PostMapping("/admin/apply")
    public String apply(AdminRequestDto requestDto, RedirectAttributes redirectAttributes) {
        adminRequestService.submit(requestDto);
        redirectAttributes.addFlashAttribute("applySuccess", true);
        return "redirect:/admin/apply";
    }

    /** 대기 중인 신청 목록 (관리자 전용) */
    @GetMapping("/Admin/admin_requests")
    public String list(Model model) {
        model.addAttribute("requestList", adminRequestService.listPending());
        return "admin/admin_requests";
    }

    @PostMapping("/Admin/admin_requests/{requestIdx}/approve")
    public String approve(@PathVariable int requestIdx, RedirectAttributes redirectAttributes) {
        adminRequestService.approve(requestIdx);
        redirectAttributes.addFlashAttribute("message", "adm.msg_approved");
        return "redirect:/Admin/admin_requests";
    }

    @PostMapping("/Admin/admin_requests/{requestIdx}/reject")
    public String reject(@PathVariable int requestIdx, RedirectAttributes redirectAttributes) {
        adminRequestService.reject(requestIdx);
        redirectAttributes.addFlashAttribute("message", "adm.msg_rejected");
        return "redirect:/Admin/admin_requests";
    }
}
