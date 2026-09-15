package com.mnu.RyokanMaker.domain;

import lombok.Data;

@Data
public class Restaurant_ReservationDTO {
	private int restaurantfacilityidx;
	private String usermail;
	private int adminidx;
	private int resv_num;
	private String restaurantusedate;
	private String restauranttimeslot;
	private String restaurantheadcount;
	private String restaurantsidemenu;
}
