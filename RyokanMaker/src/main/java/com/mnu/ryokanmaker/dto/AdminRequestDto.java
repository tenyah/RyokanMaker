package com.mnu.ryokanmaker.dto;

import java.time.LocalDateTime;

/**
 * ADMIN_REQUEST 테이블 매핑 DTO (관리자 계정 신청)
 * PK : requestIdx / 승인 시 resultAdminIdx -> ADMIN.AdminIdx
 */
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

    public AdminRequestDto() {
    }

    public Integer getRequestIdx() { return requestIdx; }
    public void setRequestIdx(Integer requestIdx) { this.requestIdx = requestIdx; }

    public String getRyokanName() { return ryokanName; }
    public void setRyokanName(String ryokanName) { this.ryokanName = ryokanName; }

    public String getApplicantName() { return applicantName; }
    public void setApplicantName(String applicantName) { this.applicantName = applicantName; }

    public String getApplicantEmail() { return applicantEmail; }
    public void setApplicantEmail(String applicantEmail) { this.applicantEmail = applicantEmail; }

    public String getApplicantTel() { return applicantTel; }
    public void setApplicantTel(String applicantTel) { this.applicantTel = applicantTel; }

    public String getRequestMessage() { return requestMessage; }
    public void setRequestMessage(String requestMessage) { this.requestMessage = requestMessage; }

    public String getRequestStatus() { return requestStatus; }
    public void setRequestStatus(String requestStatus) { this.requestStatus = requestStatus; }

    public LocalDateTime getRequestedAt() { return requestedAt; }
    public void setRequestedAt(LocalDateTime requestedAt) { this.requestedAt = requestedAt; }

    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }

    public Integer getResultAdminIdx() { return resultAdminIdx; }
    public void setResultAdminIdx(Integer resultAdminIdx) { this.resultAdminIdx = resultAdminIdx; }
}
