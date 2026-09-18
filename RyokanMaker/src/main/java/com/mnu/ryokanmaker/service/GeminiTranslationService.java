package com.mnu.ryokanmaker.service;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Locale;
import java.util.Map;

/**
 * Gemini API로 관리자가 작성한 한국어 콘텐츠(공지사항 등)를 즉석에서 번역한다.
 * 저장은 하지 않고 조회 시점에만 번역하는 방식 - 아직 콘텐츠를 저장할 때 언어별로
 * 같이 저장해두는 화면(정보등록 등)이 없어서, 우선 이 방식으로 시작한다.
 *
 * API 키가 없으면(GEMINI_API_KEY 미설정) 원문을 그대로 반환한다 - 번역 실패가
 * 사이트 전체를 죽이면 안 되기 때문.
 */
@Service
public class GeminiTranslationService {

    private static final Logger log = LoggerFactory.getLogger(GeminiTranslationService.class);
    private static final String ENDPOINT =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";

    @Value("${gemini.api.key:}")
    private String apiKey;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** targetLocale이 한국어이거나 API 키가 없으면 원문을 그대로 반환. */
    public String translate(String koreanText, Locale targetLocale) {
        if (koreanText == null || koreanText.isBlank()) {
            return koreanText;
        }
        if (targetLocale == null || Locale.KOREAN.getLanguage().equals(targetLocale.getLanguage())) {
            return koreanText;
        }
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("GEMINI_API_KEY가 설정되지 않아 번역 없이 원문을 반환합니다.");
            return koreanText;
        }

        String languageName = "en".equals(targetLocale.getLanguage()) ? "English" : "Japanese";

        try {
            String prompt = "Translate the following Korean text into " + languageName
                    + ". Only output the translated text, with no explanation or quotation marks:\n\n"
                    + koreanText;

            Map<String, Object> body = Map.of(
                    "contents", new Object[]{
                            Map.of("parts", new Object[]{Map.of("text", prompt)})
                    }
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ENDPOINT + "?key=" + apiKey))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.warn("Gemini 번역 실패 (status={}): {}", response.statusCode(), response.body());
                return koreanText;
            }

            JsonNode root = objectMapper.readTree(response.body());
            String translated = root.path("candidates").path(0).path("content")
                    .path("parts").path(0).path("text").asString(null);

            return (translated == null || translated.isBlank()) ? koreanText : translated.trim();
        } catch (Exception e) {
            log.warn("Gemini 번역 중 오류 발생, 원문을 그대로 표시합니다.", e);
            return koreanText;
        }
    }
}
