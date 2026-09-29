package com.mnu.ryokanmaker.controller;

import com.mnu.ryokanmaker.config.TenantInterceptor;
import com.mnu.ryokanmaker.domain.AdminDto;
import com.mnu.ryokanmaker.domain.NoticeDto;
import com.mnu.ryokanmaker.service.NoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 유저(비회원 포함)에게 보이는 공지사항 화면.
 * 목록 페이지는 따로 두지 않고, 메인 화면에서 최신 몇 건만 미리보기로 보여준 뒤
 * 각 항목을 상세(view)로 연결하는 방식으로 간다 (팀 논의 결과).
 * - 상세 : templates/notice/view.html
 */
@Controller
@RequestMapping("/r/{adminId}")
public class NoticeController {

    @Autowired
    private NoticeService noticeService;

    @GetMapping("/notice/view")
    public String view(@PathVariable String adminId, @RequestParam int idx, HttpServletRequest request, Model model) {
        AdminDto tenant = TenantInterceptor.currentTenant(request);
        NoticeDto notice = noticeService.select(idx);
        // 다른 료칸의 공지사항 번호를 넣어서 넘겨다보는 것을 막는다
        if (notice == null || !notice.getAdminIdx().equals(tenant.getAdminIdx())) {
            return "redirect:/r/" + adminId;
        }

        // 제목/내용은 템플릿에서 @tr.t(...)로 현재 언어에 맞춰 번역해서 보여줌
        model.addAttribute("notice", notice);
        return "notice/view";
    }
}
