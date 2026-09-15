package com.mnu.RyokanMaker.dto;

import java.time.LocalDateTime;

/**
 * NOTICE 테이블 매핑 DTO (공지사항)
 * PK : noticeIdx / FK : adminIdx -> ADMIN.AdminIdx
 */
public class NoticeDto {

    private Integer noticeIdx;
    private Integer adminIdx;
    private String noticeTitle;
    private String noticeContent;
    private LocalDateTime noticeCreatedAt;

    public NoticeDto() {
    }

    public NoticeDto(Integer noticeIdx, Integer adminIdx, String noticeTitle,
                      String noticeContent, LocalDateTime noticeCreatedAt) {
        this.noticeIdx = noticeIdx;
        this.adminIdx = adminIdx;
        this.noticeTitle = noticeTitle;
        this.noticeContent = noticeContent;
        this.noticeCreatedAt = noticeCreatedAt;
    }

    public Integer getNoticeIdx() { return noticeIdx; }
    public void setNoticeIdx(Integer noticeIdx) { this.noticeIdx = noticeIdx; }

    public Integer getAdminIdx() { return adminIdx; }
    public void setAdminIdx(Integer adminIdx) { this.adminIdx = adminIdx; }

    public String getNoticeTitle() { return noticeTitle; }
    public void setNoticeTitle(String noticeTitle) { this.noticeTitle = noticeTitle; }

    public String getNoticeContent() { return noticeContent; }
    public void setNoticeContent(String noticeContent) { this.noticeContent = noticeContent; }

    public LocalDateTime getNoticeCreatedAt() { return noticeCreatedAt; }
    public void setNoticeCreatedAt(LocalDateTime noticeCreatedAt) { this.noticeCreatedAt = noticeCreatedAt; }
}
