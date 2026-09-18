package com.mnu.ryokanmaker.controller;

import com.mnu.ryokanmaker.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 공개 교통안내 화면. 사이트가 아직 단일 료칸 기준이라 관리자 1번(DEFAULT_ADMIN_IDX)의
 * RYOKAN_ACCESS를 그대로 보여준다 (InquiryController의 DEFAULT_ADMIN_IDX와 같은 가정).
 */
@Controller
public class AccessController {

    private static final int DEFAULT_ADMIN_IDX = 1;

    @Autowired
    private AdminService adminService;

    @GetMapping("/access")
    public String access(Model model) {
        model.addAttribute("admin", adminService.findByAdminIdx(DEFAULT_ADMIN_IDX));
        return "access";
    }
}
