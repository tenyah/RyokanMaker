package com.mnu.ryokanmaker.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.mnu.ryokanmaker.domain.AdminDto;
import com.mnu.ryokanmaker.service.AdminService;

/**
 * 기존 북마크/QR코드 호환용. 손님 화면의 실제 로직은 전부 /r/{adminId} 밑에 있고,
 * "/"는 관리자 1번(맨 처음 만들어진 료칸)의 사이트로 보내는 얇은 리다이렉트만 담당한다.
 */
@Controller
public class RootController {

    private static final int DEFAULT_ADMIN_IDX = 1;

    @Autowired
    private AdminService adminService;

    @GetMapping("/")
    public String root() {
        AdminDto admin = adminService.findByAdminIdx(DEFAULT_ADMIN_IDX);
        return "redirect:/r/" + (admin != null ? admin.getAdminId() : DEFAULT_ADMIN_IDX);
    }
}
