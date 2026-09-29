package com.mnu.ryokanmaker.controller;

import com.mnu.ryokanmaker.config.TenantInterceptor;
import com.mnu.ryokanmaker.domain.AdminDto;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 공개 교통안내 화면. /r/{adminId} 밑이라 어느 료칸인지는 TenantInterceptor가 미리 찾아둔다.
 */
@Controller
@RequestMapping("/r/{adminId}")
public class AccessController {

    @GetMapping("/access")
    public String access(@PathVariable String adminId, HttpServletRequest request, Model model) {
        AdminDto admin = TenantInterceptor.currentTenant(request);
        model.addAttribute("admin", admin);
        return "access";
    }
}
