package com.mnu.RyokanMaker.dto;

/**
 * RESERVATION 테이블 매핑 DTO (예약 1건의 상위/통합 정보 - 결제 상태 등)
 * PK : resvNum / FK : adminIdx -> ADMIN.AdminIdx, userMail -> MEMBER.UserMail
 */
public class ReservationDto {

    private Integer resvNum;
    private Integer adminIdx;
    private String userMail;
    private Integer resvPrice;
    private Integer resvPeople;
    private String resvStatus;
    private String resvPayStatus;
    private String resvPayMethod;
    private String resvOrderId; // 결제 PG 주문번호

    public ReservationDto() {
    }

    public ReservationDto(Integer resvNum, Integer adminIdx, String userMail, Integer resvPrice,
                           Integer resvPeople, String resvStatus, String resvPayStatus, String resvPayMethod,
                           String resvOrderId) {
        this.resvNum = resvNum;
        this.adminIdx = adminIdx;
        this.userMail = userMail;
        this.resvPrice = resvPrice;
        this.resvPeople = resvPeople;
        this.resvStatus = resvStatus;
        this.resvPayStatus = resvPayStatus;
        this.resvPayMethod = resvPayMethod;
        this.resvOrderId = resvOrderId;
    }

    public Integer getResvNum() { return resvNum; }
    public void setResvNum(Integer resvNum) { this.resvNum = resvNum; }

    public Integer getAdminIdx() { return adminIdx; }
    public void setAdminIdx(Integer adminIdx) { this.adminIdx = adminIdx; }

    public String getUserMail() { return userMail; }
    public void setUserMail(String userMail) { this.userMail = userMail; }

    public Integer getResvPrice() { return resvPrice; }
    public void setResvPrice(Integer resvPrice) { this.resvPrice = resvPrice; }

    public Integer getResvPeople() { return resvPeople; }
    public void setResvPeople(Integer resvPeople) { this.resvPeople = resvPeople; }

    public String getResvStatus() { return resvStatus; }
    public void setResvStatus(String resvStatus) { this.resvStatus = resvStatus; }

    public String getResvPayStatus() { return resvPayStatus; }
    public void setResvPayStatus(String resvPayStatus) { this.resvPayStatus = resvPayStatus; }

    public String getResvPayMethod() { return resvPayMethod; }
    public void setResvPayMethod(String resvPayMethod) { this.resvPayMethod = resvPayMethod; }

    public String getResvOrderId() { return resvOrderId; }
    public void setResvOrderId(String resvOrderId) { this.resvOrderId = resvOrderId; }
}
