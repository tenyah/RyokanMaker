package com.mnu.ryokanmaker.domain;
import lombok.Data;
import java.time.LocalDate;

@Data
public class RoomReservationDTO {
    private Long roomResvNum;
    private String userMail;
    private Long adminIdx;
    private Long roomIdx;
    private Long resvNum;
    private Long planIdx;
    private LocalDate resvCheckIn;
    private LocalDate resvCheckOut;
    private Long resvPrice;
    private Long resvPeople;
    private String resvStatus;
    private String resvPayStatus;
    private String resvPayMethod;
}