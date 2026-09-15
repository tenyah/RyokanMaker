package com.mnu.RyokanMaker.domain;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class NoticeDTO {
    private int noticeIdx;
    private int adminIdx;
    private String noticeTitle;
    private String noticeContent;
    private LocalDateTime noticeCreatedAt;

}
