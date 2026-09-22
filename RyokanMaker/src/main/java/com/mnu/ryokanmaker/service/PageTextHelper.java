package com.mnu.ryokanmaker.service;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

/**
 * 템플릿에서 `${@pt.t('KEY')}` 로 쓰는 관리자 수정 가능 문구 도우미.
 * 관리자가 저장한 문구(한국어 원문)가 있으면 현재 언어로 번역해서, 없으면 messages의 기본 문구를 보여준다.
 * 줄바꿈이 있는 문구는 화면 쪽에서 white-space: pre-line 으로 표시한다.
 */
@Component("pt")
public class PageTextHelper {

    @Autowired
    private PageContentService pageContentService;

    @Autowired
    private GeminiTranslationService translationService;

    @Autowired
    private MessageSource messageSource;

    public String t(String key) {
        Locale locale = LocaleContextHolder.getLocale();
        String custom = pageContentService.getSiteMap().get(key);
        if (custom != null) {
            return translationService.translate(custom, locale);
        }
        return defaultMessage(key, locale);
    }

    /** 관리자 입력칸의 placeholder용 기본 문구 */
    public String placeholder(String key) {
        return defaultMessage(key, LocaleContextHolder.getLocale());
    }

    private String defaultMessage(String key, Locale locale) {
        return PageTextDefs.find(key)
                .map(def -> messageSource.getMessage(def.messageKey(), null, def.messageKey(), locale))
                .orElse("");
    }
}
