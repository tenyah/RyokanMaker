package com.mnu.RyokanMaker.dto;

import java.time.LocalDateTime;

/**
 * INQUIRY 테이블 매핑 DTO (1:1 문의)
 * PK : inquiryIdx / FK : adminIdx -> ADMIN.AdminIdx, userMail -> MEMBER.UserMail
 */
public class InquiryDto {

    private Integer inquiryIdx;
    private Integer adminIdx;
    private String userMail;
    private String inquiryTitle;
    private String inquiryContent;
    private String inquiryAnswerContent;
    private String inquiryStatus;
    private LocalDateTime inquiryCreatedAt;

    public InquiryDto() {
    }

    public InquiryDto(Integer inquiryIdx, Integer adminIdx, String userMail, String inquiryTitle,
                       String inquiryContent, String inquiryAnswerContent, String inquiryStatus,
                       LocalDateTime inquiryCreatedAt) {
        this.inquiryIdx = inquiryIdx;
        this.adminIdx = adminIdx;
        this.userMail = userMail;
        this.inquiryTitle = inquiryTitle;
        this.inquiryContent = inquiryContent;
        this.inquiryAnswerContent = inquiryAnswerContent;
        this.inquiryStatus = inquiryStatus;
        this.inquiryCreatedAt = inquiryCreatedAt;
    }

    public Integer getInquiryIdx() { return inquiryIdx; }
    public void setInquiryIdx(Integer inquiryIdx) { this.inquiryIdx = inquiryIdx; }

    public Integer getAdminIdx() { return adminIdx; }
    public void setAdminIdx(Integer adminIdx) { this.adminIdx = adminIdx; }

    public String getUserMail() { return userMail; }
    public void setUserMail(String userMail) { this.userMail = userMail; }

    public String getInquiryTitle() { return inquiryTitle; }
    public void setInquiryTitle(String inquiryTitle) { this.inquiryTitle = inquiryTitle; }

    public String getInquiryContent() { return inquiryContent; }
    public void setInquiryContent(String inquiryContent) { this.inquiryContent = inquiryContent; }

    public String getInquiryAnswerContent() { return inquiryAnswerContent; }
    public void setInquiryAnswerContent(String inquiryAnswerContent) { this.inquiryAnswerContent = inquiryAnswerContent; }

    public String getInquiryStatus() { return inquiryStatus; }
    public void setInquiryStatus(String inquiryStatus) { this.inquiryStatus = inquiryStatus; }

    public LocalDateTime getInquiryCreatedAt() { return inquiryCreatedAt; }
    public void setInquiryCreatedAt(LocalDateTime inquiryCreatedAt) { this.inquiryCreatedAt = inquiryCreatedAt; }
}
