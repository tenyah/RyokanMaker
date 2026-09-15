package com.mnu.RyokanMaker.domain;

import lombok.Data;

@Data
public class ReservationDTO {
	private int resv_num;
	private int adminidx;
	private String usermail;
	private int resvPrice;
	private int resvpeople;
	private String resvStatus;
	private String resvpayStatus;
	private String resvpaymethod;
	private String resvArrivalTime; // 도착 예정 시간 (선택)
	private String resvRequest;
	private String resvOrderId; // 결제 PG 주문번호


}
