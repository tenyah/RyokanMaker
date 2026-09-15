package com.mnu.RyokanMaker.domain;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class InquiryDTO {
    private int inquiryIdx;
    private int adminIdx;
    private String userMail;
    private String inquiryTitle;
    private String inquiryContent;
    private String inquiryAnswerContent;
    private String inquiryStatus;
    private LocalDateTime inquiryCreatedAt;


}
