package com.mnu.ryokanmaker.domain;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * NOTICE 테이블 매핑 DTO (공지사항)
 * PK : noticeIdx (IDENTITY, 자동 채번) / FK : adminIdx -> ADMIN.ADMIN_IDX
 * noticeCreatedAt은 등록 시 DB에서 SYSTIMESTAMP로 자동 기록 (화면/자바에서 값을 넣지 않음). 수정 시에도 값이 바뀌지 않음.
 */
@Data
public class NoticeDto {

    private Integer noticeIdx;
    private Integer adminIdx;
    private String noticeTitle;
    private String noticeContent;
    private LocalDateTime noticeCreatedAt;
}
