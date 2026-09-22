package com.mnu.maven.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MavenController {
	//JSP 동작 테스트
	@GetMapping("/Test")
	public String test() {
		return "test"; //jsp 파일명
	}
	
	//JSTL 테스트
	@GetMapping("/Exam")
	public String exam() {
		return "exam"; //jsp 파일명
	}
}
