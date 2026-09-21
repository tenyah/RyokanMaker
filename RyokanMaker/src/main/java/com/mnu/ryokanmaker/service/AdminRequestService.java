package com.mnu.ryokanmaker.service;

import com.mnu.ryokanmaker.dto.AdminDto;
import com.mnu.ryokanmaker.dto.AdminRequestDto;
import com.mnu.ryokanmaker.mapper.AdminMapper;
import com.mnu.ryokanmaker.mapper.AdminRequestMapper;
import com.mnu.ryokanmaker.util.PasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 관리자 계정 신청 → 승인 대기 → 승인 시 계정 발급 + 메일 발송 흐름.
 * (팀 논의: 관리자 아이디/비밀번호는 자율 가입이 아니라 신청 후 운영진이 승인하는 방식)
 */
@Service
public class AdminRequestService {

    private static final Logger log = LoggerFactory.getLogger(AdminRequestService.class);

    @Autowired
    private AdminRequestMapper adminRequestMapper;

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private EmailService emailService;

    public void submit(AdminRequestDto requestDto) {
        adminRequestMapper.insert(requestDto);
        try {
            emailService.sendAdminRequestNotification(requestDto.getRyokanName(), requestDto.getApplicantName(),
                    requestDto.getApplicantEmail(), requestDto.getApplicantTel(), requestDto.getRequestMessage());
        } catch (Exception e) {
            // 알림 메일 발송 실패로 신청 접수 자체가 실패하면 안 되므로 로그만 남기고 넘어간다.
            log.warn("관리자 계정 신청 알림 메일 발송 실패", e);
        }
    }

    public List<AdminRequestDto> listPending() {
        return adminRequestMapper.selectPending();
    }

    /** 신청 승인 : ADMIN 계정 생성 + 신청 상태 갱신 + 메일 발송 */
    public void approve(int requestIdx) {
        AdminRequestDto request = adminRequestMapper.selectByIdx(requestIdx);
        if (request == null || !"대기".equals(request.getRequestStatus())) {
            throw new IllegalStateException("이미 처리되었거나 존재하지 않는 신청입니다.");
        }

        String adminId = generateUniqueAdminId(requestIdx);
        String tempPassword = PasswordUtil.generateTempPassword();

        AdminDto adminDto = new AdminDto();
        adminDto.setAdminId(adminId);
        adminDto.setAdminPassword(tempPassword);
        adminDto.setAdminName(request.getApplicantName());
        adminDto.setAdminMail(request.getApplicantEmail());
        adminDto.setRyokanName(request.getRyokanName());
        adminDto.setPwResetYn("N"); // 임시 비밀번호(평문)이므로 최초 로그인 시 비밀번호 변경을 강제
        adminMapper.insert(adminDto);

        adminRequestMapper.approve(requestIdx, adminDto.getAdminIdx());
        emailService.sendAdminCredentials(request.getApplicantEmail(), request.getRyokanName(), adminId, tempPassword);
    }

    public void reject(int requestIdx) {
        AdminRequestDto request = adminRequestMapper.selectByIdx(requestIdx);
        if (request == null || !"대기".equals(request.getRequestStatus())) {
            throw new IllegalStateException("이미 처리되었거나 존재하지 않는 신청입니다.");
        }
        adminRequestMapper.reject(requestIdx);
        emailService.sendAdminRequestRejected(request.getApplicantEmail(), request.getRyokanName());
    }

    private String generateUniqueAdminId(int requestIdx) {
        String candidate = "ryokan" + requestIdx;
        while (adminMapper.selectByAdminId(candidate) != null) {
            candidate = candidate + "x";
        }
        return candidate;
    }
}
