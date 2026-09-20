package com.mnu.ryokanmaker.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

/**
 * Thymeleaf 템플릿에서 `${@tr.t(값)}` 형태로 쓰는 번역 도우미.
 *
 * - t(text)                : DB에서 온 자유 텍스트를 현재 언어로 번역 (원문이 KO/EN/JA 어느 것이든)
 * - prefetch(목록, 속성명...)  : 목록 화면에서 여러 항목의 텍스트를 API 1회로 미리 번역해 캐시에 넣는다
 * - label(접두어, 값)        : DB에 저장된 상태값('답변대기' 등)을 messages의 정해진 번역으로 표시
 */
@Component("tr")
public class TranslationHelper {

    @Autowired
    private GeminiTranslationService translationService;

    @Autowired
    private MessageSource messageSource;

    public String t(String text) {
        return translationService.translate(text, LocaleContextHolder.getLocale());
    }

    /** 화면에 아무것도 출력하지 않도록 빈 문자열을 반환한다 (th:with 등에서 호출용). */
    public String prefetch(Collection<?> items, String... properties) {
        if (items == null || items.isEmpty()) {
            return "";
        }
        List<String> texts = new ArrayList<>();
        for (Object item : items) {
            if (item == null) {
                continue;
            }
            if (item instanceof String s) {
                texts.add(s);
                continue;
            }
            BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(item);
            for (String property : properties) {
                if (wrapper.isReadableProperty(property)
                        && wrapper.getPropertyValue(property) instanceof String value) {
                    texts.add(value);
                }
            }
        }
        translationService.translateAll(texts, LocaleContextHolder.getLocale());
        return "";
    }

    /** 상세 화면처럼 개별 텍스트 몇 개를 API 1회로 미리 번역해 둔다. null은 무시한다. */
    public String prefetchAll(String... texts) {
        List<String> list = new ArrayList<>();
        for (String text : texts) {
            if (text != null) {
                list.add(text);
            }
        }
        translationService.translateAll(list, LocaleContextHolder.getLocale());
        return "";
    }

    /** messages에 `접두어.값` 키가 있으면 그 번역을, 없으면 값을 그대로 번역해서 반환. */
    public String label(String prefix, String value) {
        if (value == null) {
            return null;
        }
        Locale locale = LocaleContextHolder.getLocale();
        try {
            return messageSource.getMessage(prefix + "." + value, null, locale);
        } catch (NoSuchMessageException e) {
            return t(value);
        }
    }
}
