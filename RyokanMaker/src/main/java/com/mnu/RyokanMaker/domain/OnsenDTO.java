package com.mnu.RyokanMaker.domain;

import lombok.Data;

@Data
public class OnsenDTO {
	private int onsenidx;
	private String onsenname;
	private String onsenimage;
	private String onseninfo;
	private int adminidx;
	private String onsenSaleYn;
	private String onsenMemo;
}
