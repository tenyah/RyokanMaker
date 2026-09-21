package com.mnu.ryokanmaker.config;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

/**
 * 다국어(i18n) 설정.
 * - 기본 언어는 한국어, 세션에 저장해서 페이지를 넘어가도 선택한 언어가 유지됨
 * - URL에 ?lang=en / ?lang=ja / ?lang=ko 를 붙이면 언어가 바뀜 (헤더의 언어 전환 버튼이 이 방식 사용)
 *
 * 업로드된 이미지는 target/classes가 아니라 소스 트리(src/main/resources/static/uploads)에
 * 직접 저장한다 (재빌드/mvn clean에도 파일이 안 사라지도록). Spring의 기본 정적 리소스 매핑은
 * classpath:/static/만 보므로, /uploads/** 요청을 이 폴더로 직접 매핑해서 서빙한다.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver resolver = new SessionLocaleResolver();
        resolver.setDefaultLocale(Locale.KOREAN);
        return resolver;
    }

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang");
        return interceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadRoot = Paths.get("src/main/resources/static/uploads").toAbsolutePath().normalize();
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadRoot + "/");
    }
}
