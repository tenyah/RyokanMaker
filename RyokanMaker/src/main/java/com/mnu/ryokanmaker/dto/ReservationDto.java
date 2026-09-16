package com.mnu.ryokanmaker.dto;

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
}
