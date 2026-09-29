package com.mnu.ryokanmaker.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.util.UrlPathHelper;

import com.mnu.ryokanmaker.domain.AdminDto;
import com.mnu.ryokanmaker.service.AdminService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Map;

/**
 * 손님용 공개 화면은 전부 /r/{adminId}/... 밑에 있다. 이 인터셉터가 경로의 adminId(ADMIN.ADMIN_ID)로
 * 어느 료칸인지 미리 찾아서 request attribute(TENANT_ATTR)에 넣어두면, GlobalModelAdvice가 이걸
 * "tenant" 모델 속성으로 모든 뷰에 노출한다. 존재하지 않는 adminId면 404.
 */
@Component
public class TenantInterceptor implements HandlerInterceptor {

    public static final String TENANT_ATTR = "resolvedTenant";

    @Autowired
    private AdminService adminService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        @SuppressWarnings("unchecked")
        Map<String, String> pathVars = (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        String adminId = pathVars == null ? null : pathVars.get("adminId");
        if (adminId == null) {
            // /r/{adminId}로 매핑되지 않은 요청 (정적 리소스 등) - 통과
            return true;
        }

        AdminDto tenant = adminService.findByAdminId(adminId);
        if (tenant == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "존재하지 않는 료칸입니다: " + adminId);
            return false;
        }
        tenant.setAdminPassword(null); // 모델/템플릿에 절대 노출하지 않음
        request.setAttribute(TENANT_ATTR, tenant);
        return true;
    }

    /** 컨트롤러가 매번 @PathVariable + AdminService 조회를 반복하지 않도록 꺼내 쓰는 헬퍼. */
    public static AdminDto currentTenant(HttpServletRequest request) {
        return (AdminDto) request.getAttribute(TENANT_ATTR);
    }

    /**
     * 회원 로그인 세션 키를 료칸(adminId)별로 분리한다. 같은 브라우저의 세션 쿠키는 경로와 무관하게
     * 도메인 전체에서 공유되므로, 키 자체를 분리하지 않으면 한 세션에서 두 료칸에 각각 로그인했을 때
     * 서로 덮어쓴다(예: 두 탭에서 /r/a, /r/b에 각각 로그인).
     */
    public static String memberSessionKey(String adminId) {
        return "loginMember:" + adminId;
    }
}
