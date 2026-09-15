package com.mnu.RyokanMaker.domain;

import lombok.Data;

@Data
public class AdminDTO {
	private int adminidx;
	private String adminid;
	private String adminpassword;
	private String adminname;
	private String adminmail;
	private String ryokanloc;
	private String ryokanname;
	private String ryokanfacility;
	private String ryokantel;
	private String ryokanaccess;
	private String ryokanlogo;
	private String pwresetyn;
}
