package com.mnu.ryokanmaker.service;

import com.mnu.ryokanmaker.dto.AdminDto;
import com.mnu.ryokanmaker.mapper.AdminMapper;
import com.mnu.ryokanmaker.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

    @Autowired
    private AdminMapper adminMapper;

    /** 로그인 : 아이디/비밀번호가 맞으면 관리자 정보를, 아니면 null을 반환 */
    public AdminDto authenticate(String adminId, String adminPassword) {
        AdminDto admin = adminMapper.selectByAdminId(adminId);
        if (admin == null) {
            return null;
        }
        if (!admin.getAdminPassword().equals(PasswordUtil.sha256(adminPassword))) {
            return null;
        }
        return admin;
    }

    /** 비밀번호 변경 : 현재 비밀번호가 맞으면 새 비밀번호로 바꾸고 true, 아니면 false */
    public boolean changePassword(Integer adminIdx, String currentPassword, String newPassword) {
        AdminDto admin = adminMapper.selectByAdminIdx(adminIdx);
        if (admin == null || !admin.getAdminPassword().equals(PasswordUtil.sha256(currentPassword))) {
            return false;
        }
        adminMapper.updatePassword(adminIdx, PasswordUtil.sha256(newPassword));
        return true;
    }

    public AdminDto findByAdminIdx(Integer adminIdx) {
        return adminMapper.selectByAdminIdx(adminIdx);
    }

    public void updateAccess(Integer adminIdx, String ryokanAccess) {
        adminMapper.updateAccess(adminIdx, ryokanAccess);
    }
}
