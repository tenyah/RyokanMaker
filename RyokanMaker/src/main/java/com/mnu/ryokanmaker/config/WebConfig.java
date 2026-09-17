package com.mnu.ryokanmaker.config;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 업로드된 이미지는 target/classes가 아니라 소스 트리(src/main/resources/static/uploads)에
 * 직접 저장한다 (재빌드/mvn clean에도 파일이 안 사라지도록). Spring의 기본 정적 리소스 매핑은
 * classpath:/static/만 보므로, /uploads/** 요청을 이 폴더로 직접 매핑해서 서빙한다.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		Path uploadRoot = Paths.get("src/main/resources/static/uploads").toAbsolutePath().normalize();
		registry.addResourceHandler("/uploads/**")
				.addResourceLocations("file:" + uploadRoot + "/");
	}
}
