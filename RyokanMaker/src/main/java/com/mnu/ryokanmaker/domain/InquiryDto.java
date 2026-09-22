package com.mnu.ryokanmaker.domain;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * INQUIRY 테이블 매핑 DTO (1:1 문의)
 * PK : inquiryIdx / FK : adminIdx -> ADMIN.ADMIN_IDX, userMail -> MEMBER.USER_MAIL
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
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
