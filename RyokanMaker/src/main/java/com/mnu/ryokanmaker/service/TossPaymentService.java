package com.mnu.ryokanmaker.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

/** 토스페이먼츠 결제 승인(confirm) API 서버 사이드 호출 담당 */
@Service
public class TossPaymentService {

    private final RestClient restClient;

    @Value("${tosspayments.secret-key}")
    private String secretKey;

    public TossPaymentService() {
        this.restClient = RestClient.create("https://api.tosspayments.com");
    }

    /**
     * 결제 승인 API 호출. 호출 전에 반드시 서버에 저장된 주문 금액과 amount가
     * 일치하는지 검증한 뒤 호출해야 한다 (successUrl의 amount는 위변조 가능).
     */
    public Map<String, Object> confirm(String paymentKey, String orderId, int amount) {
        String encodedAuth = Base64.getEncoder()
                .encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));

        try {
            return restClient.post()
                    .uri("/v1/payments/confirm")
                    .header(HttpHeaders.AUTHORIZATION, "Basic " + encodedAuth)
                    .header("Idempotency-Key", UUID.randomUUID().toString())
                    .body(Map.of(
                            "paymentKey", paymentKey,
                            "orderId", orderId,
                            "amount", amount
                    ))
                    .retrieve()
                    .body(Map.class);
        } catch (RestClientResponseException e) {
            throw new IllegalStateException("결제 승인 실패: " + e.getResponseBodyAsString(), e);
        }
    }
}
