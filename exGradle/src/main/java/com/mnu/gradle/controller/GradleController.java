package com.mnu.gradle.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class GradleController {
	@GetMapping("/aaa")
	public String test() {
		return "test";
	}
	
	@GetMapping("/bbb")
	public String exam() {
		return "exam";
	}
	
}
