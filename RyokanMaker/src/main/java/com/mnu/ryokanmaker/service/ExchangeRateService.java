package com.mnu.ryokanmaker.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import jakarta.annotation.PostConstruct;

/**
 * 엔화(JPY, DB에 저장된 가격 단위) 기준 환율을 하루 1회 조회해서 메모리에 캐싱한다.
 * 조회 실패 시 직전 값(최초 실패면 대략적인 고정값)을 그대로 유지한다.
 *
 * 실제 결제(TossPayments)는 항상 원화(KRW)로만 처리되므로, 이 서비스는
 * "화면 표시용 환산"과 "결제 시 KRW 환산" 양쪽에 같은 환율을 사용한다.
 */
@Service
public class ExchangeRateService {

    private static final Logger log = LoggerFactory.getLogger(ExchangeRateService.class);

    private static final String API_URL = "https://api.frankfurter.dev/v1/latest?base=JPY&symbols=KRW,USD";

    // API를 아직 한 번도 못 불러왔을 때 쓰는 대략적인 기본값 (100엔 기준)
    private static final BigDecimal DEFAULT_KRW = new BigDecimal("9.2");
    private static final BigDecimal DEFAULT_USD = new BigDecimal("0.0067");

    private final RestClient restClient = RestClient.create();
    private final Map<String, BigDecimal> rates = new ConcurrentHashMap<>(Map.of(
            "KRW", DEFAULT_KRW,
            "USD", DEFAULT_USD
    ));

    @PostConstruct
    @SuppressWarnings("unchecked")
    public void refresh() {
        try {
            Map<String, Object> body = restClient.get()
                    .uri(API_URL)
                    .retrieve()
                    .body(Map.class);

            Map<String, Object> newRates = (Map<String, Object>) body.get("rates");
            BigDecimal krw = new BigDecimal(newRates.get("KRW").toString());
            BigDecimal usd = new BigDecimal(newRates.get("USD").toString());

            rates.put("KRW", krw);
            rates.put("USD", usd);
            log.info("환율 갱신 완료: 1엔 = {}원, {}달러", krw, usd);
        } catch (Exception e) {
            log.warn("환율 API 조회 실패 - 직전(또는 기본) 환율을 계속 사용합니다.", e);
        }
    }

    /** 매일 새벽 3시에 자동 갱신 */
    @Scheduled(cron = "0 0 3 * * *")
    public void scheduledRefresh() {
        refresh();
    }

    /** 엔화 금액 -> 원화 정수 (반올림) */
    public long toKrw(long amountJpy) {
        return BigDecimal.valueOf(amountJpy).multiply(rates.get("KRW"))
                .setScale(0, RoundingMode.HALF_UP).longValue();
    }

    /** 엔화 금액 -> 달러 (소수점 2자리 반올림) */
    public BigDecimal toUsd(long amountJpy) {
        return BigDecimal.valueOf(amountJpy).multiply(rates.get("USD"))
                .setScale(2, RoundingMode.HALF_UP);
    }
}
