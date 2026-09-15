package com.mnu.RyokanMaker.domain;

import java.time.LocalDate;

import lombok.Data;

@Data
public class Room_ReservationDTO {
    private int roomResvNum;
    private String userMail;
    private int adminIdx;
    private int roomIdx;
    private int resvNum;
    private int planIdx;
    private LocalDate resvCheckIn;
    private LocalDate resvCheckOut;
    private int resvPrice;
    private int resvPeople;
    private String resvStatus;
    private String resvPayStatus;
    private String resvPayMethod;

}
