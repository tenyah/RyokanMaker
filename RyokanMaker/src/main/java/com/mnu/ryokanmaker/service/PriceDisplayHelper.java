package com.mnu.ryokanmaker.service;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

/**
 * Thymeleaf 템플릿에서 `${@price.format(값)}` 형태로 쓰는 통화 표시 도우미.
 * DB의 가격은 전부 엔화(JPY) 기준으로 저장되어 있다는 전제로,
 * 현재 언어에 맞춰 엔/원/달러로 환산해서 보여준다 (둘러보는 화면 전용 — 결제 화면은 항상 원화).
 */
@Component("price")
public class PriceDisplayHelper {

    @Autowired
    private ExchangeRateService exchangeRateService;

    public String format(Number amountJpy) {
        long jpy = amountJpy == null ? 0L : amountJpy.longValue();
        Locale locale = LocaleContextHolder.getLocale();

        return switch (locale.getLanguage()) {
            case "ko" -> "₩" + String.format(Locale.US, "%,d", exchangeRateService.toKrw(jpy));
            case "en" -> "$" + String.format(Locale.US, "%,.2f", exchangeRateService.toUsd(jpy));
            default -> "¥" + String.format(Locale.US, "%,d", jpy); // ja 및 그 외 언어는 원본(엔화) 그대로
        };
    }
}
