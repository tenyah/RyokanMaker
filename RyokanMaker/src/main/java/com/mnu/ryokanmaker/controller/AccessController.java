package com.mnu.ryokanmaker.controller;

import com.mnu.ryokanmaker.dto.AdminDto;
import com.mnu.ryokanmaker.service.AdminService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(AccessController.class);
    private static final int DEFAULT_ADMIN_IDX = 1;

    @Autowired
    private AdminService adminService;

    @GetMapping("/access")
    public String access(Model model) {
        AdminDto admin = null;
        try {
            admin = adminService.findByAdminIdx(DEFAULT_ADMIN_IDX);
        } catch (Exception e) {
            // DB 연결 문제로 교통안내 문구 하나 때문에 페이지 전체가 죽지 않도록 방어
            log.warn("교통안내 조회 실패 - 기본 안내문으로 표시합니다.", e);
        }
        model.addAttribute("admin", admin);
        return "access";
    }
}
