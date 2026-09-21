package com.mnu.ryokanmaker.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * NOTICE 테이블 매핑 DTO (공지사항)
 * PK : noticeIdx / FK : adminIdx -> ADMIN.ADMIN_IDX
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoticeDto {

    private Integer noticeIdx;
    private Integer adminIdx;
    private String noticeTitle;
    private String noticeContent;
    private LocalDateTime noticeCreatedAt;
}
