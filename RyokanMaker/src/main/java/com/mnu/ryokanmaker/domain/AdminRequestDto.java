package com.mnu.ryokanmaker.domain;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ADMIN_REQUEST 테이블 매핑 DTO (관리자 계정 신청)
 * PK : requestIdx / 승인 시 resultAdminIdx -> ADMIN.AdminIdx
 */
@Data
@NoArgsConstructor
public class AdminRequestDto {

    private Integer requestIdx;
    private String ryokanName;
    private String applicantName;
    private String applicantEmail;
    private String applicantTel;
    private String requestMessage;
    private String requestStatus;
    private LocalDateTime requestedAt;
    private LocalDateTime processedAt;
    private Integer resultAdminIdx;
}
