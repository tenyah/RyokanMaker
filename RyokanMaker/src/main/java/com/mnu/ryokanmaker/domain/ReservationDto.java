package com.mnu.ryokanmaker.domain;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * RESERVATION 테이블 매핑 DTO (예약 1건의 상위/통합 정보 - 결제 상태 등)
 * PK : resvNum / FK : adminIdx -> ADMIN.ADMIN_IDX, userMail -> MEMBER.USER_MAIL
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationDto {

    private Integer resvNum;
    private Integer adminIdx;
    private String userMail;
    private Integer resvPrice;
    private Integer resvPeople;
    private String resvStatus;
    private String resvPayStatus;
    private String resvPayMethod;
    private String resvOrderId;
    private String resvArrivalTime;
    private String resvRequest;
    private LocalDate resvDay;
    // 예약자(실제 숙박객) 정보. 예약한 회원(userMail)과 다른 사람일 수 있다. 영문 이름·메일은 NOT NULL, 일본어 이름은 선택.
    private String resvLastNameEn;
    private String resvFirstNameEn;
    private String resvLastNameJp;
    private String resvFirstNameJp;
    private String resvMail;
    private String resvCountry;  // COUNTRY_CODE.COUNTRY_NAME (FK)
    private String resvTel;
}
