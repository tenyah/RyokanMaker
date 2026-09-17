package com.mnu.ryokanmaker.domain;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * INQUIRY 테이블 매핑 DTO (1:1 문의)
 * PK : inquiryIdx (IDENTITY, 자동 채번) / FK : adminIdx -> ADMIN.ADMIN_IDX, userMail -> MEMBER.USER_MAIL
 * inquiryStatus : '답변대기' | '답변완료'. 등록 시 '답변대기'로 고정, 관리자가 답변 등록 시 '답변완료'로 갱신.
 */
@Data
public class InquiryDto {

    private Integer inquiryIdx;
    private Integer adminIdx;
    private String userMail;
    private String inquiryTitle;
    private String inquiryContent;
    private String inquiryAnswerContent;
    private String inquiryStatus;
    private LocalDateTime inquiryCreatedAt;
}
