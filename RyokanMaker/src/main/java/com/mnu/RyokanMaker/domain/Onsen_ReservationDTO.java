package com.mnu.RyokanMaker.domain;

import java.time.LocalDate;

import lombok.Data;

@Data
public class Onsen_ReservationDTO {
	private int OnsenFacilityidx;
	private int adminidx;
	private String usermail;
	private int resv_num;
	private int onsenidx;

	private LocalDate onsenusedate;
	private String OnsenTimeSlot;
	private String OnsenHeadcount;
	private String OnsenStatus;
}
