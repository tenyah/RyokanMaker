package com.mnu.ryokanmaker.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Gemini API로 DB에 저장된 콘텐츠(관리자가 쓴 공지/객실/플랜 설명, 회원의 문의/답변 등)를
 * 보는 사람의 언어(KO/EN/JA)로 즉석 번역한다.
 *
 * - 원문 언어는 한국어/영어/일본어 어느 것이어도 된다(Gemini가 자동 감지).
 * - 이미 목표 언어인 글은 API를 부르지 않는다(문자 종류로 판별).
 * - 같은 (언어, 원문)은 캐시하고, 캐시는 파일에도 저장해서 서버를 재시작해도 다시 번역하지 않는다.
 * - 여러 문장은 한 번의 호출로 묶어서 번역한다(translateAll).
 * - 모델이 번역하지 않고 원문을 그대로 돌려주면 실패로 보고 다음 모델로 재시도한다.
 * - API 키가 없거나 호출이 실패하면 원문을 그대로 반환한다. 번역 실패가 화면을 깨면 안 되기 때문.
 */
@Service
public class GeminiTranslationService {

    private static final Logger log = LoggerFactory.getLogger(GeminiTranslationService.class);
    private static final int CACHE_MAX = 5000;
    private static final int BATCH_MAX = 40;
    private static final long FAIL_COOLDOWN_MS = 60_000;

    @Value("${gemini.api.key:}")
    private String apiKey;

    // 앞에서부터 시도하고, 과부하(503)/한도초과(429)/폐기(404) 등으로 실패하면 다음 모델로 넘어간다.
    @Value("${gemini.models:gemini-flash-lite-latest,gemini-3.1-flash-lite,gemini-flash-latest}")
    private List<String> models;

    @Value("${gemini.endpoint:https://generativelanguage.googleapis.com/v1beta/models}")
    private String endpoint;

    @Value("${gemini.cache-file:translation-cache.json}")
    private String cacheFile;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Map<String, String> cache = Collections.synchronizedMap(
            new LinkedHashMap<>(256, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
                    return size() > CACHE_MAX;
                }
            });

    private volatile long failedUntil = 0;

    @PostConstruct
    void loadCache() {
        Path path = Paths.get(cacheFile);
        if (!Files.exists(path)) {
            return;
        }
        try {
            JsonNode root = objectMapper.readTree(Files.readString(path, StandardCharsets.UTF_8));
            root.properties().forEach(e -> cache.put(e.getKey(), e.getValue().asString("")));
            log.info("번역 캐시 {}건을 {}에서 불러왔습니다.", cache.size(), path.toAbsolutePath());
        } catch (Exception e) {
            log.warn("번역 캐시 파일을 읽지 못해 빈 캐시로 시작합니다: {}", path.toAbsolutePath(), e);
        }
    }

    private void saveCache() {
        try {
            Map<String, String> snapshot;
            synchronized (cache) {
                snapshot = new LinkedHashMap<>(cache);
            }
            Path path = Paths.get(cacheFile);
            Path tmp = Paths.get(cacheFile + ".tmp");
            Files.writeString(tmp, objectMapper.writeValueAsString(snapshot), StandardCharsets.UTF_8);
            Files.move(tmp, path, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            log.warn("번역 캐시 파일 저장에 실패했습니다 (번역 자체는 계속 동작합니다).", e);
        }
    }

    /** 한 문장 번역. 번역할 필요가 없거나 실패하면 원문 그대로. */
    public String translate(String text, Locale targetLocale) {
        if (text == null) {
            return null;
        }
        return translateAll(List.of(text), targetLocale).get(0);
    }

    /** 여러 문장을 한 번에 번역. 입력과 같은 길이/순서의 리스트를 반환한다. */
    public List<String> translateAll(List<String> texts, Locale targetLocale) {
        String lang = languageOf(targetLocale);
        List<String> result = new ArrayList<>(texts);

        Set<String> pending = new LinkedHashSet<>();
        for (String text : texts) {
            if (needsTranslation(text, lang) && !cache.containsKey(cacheKey(lang, text))) {
                pending.add(text);
            }
        }

        if (!pending.isEmpty() && canCallApi()) {
            List<String> batch = new ArrayList<>();
            for (String text : pending) {
                batch.add(text);
                if (batch.size() == BATCH_MAX) {
                    callGemini(batch, lang);
                    batch = new ArrayList<>();
                }
            }
            if (!batch.isEmpty()) {
                callGemini(batch, lang);
            }
        }

        for (int i = 0; i < texts.size(); i++) {
            String text = texts.get(i);
            if (needsTranslation(text, lang)) {
                String cached = cache.get(cacheKey(lang, text));
                if (cached != null) {
                    result.set(i, cached);
                }
            }
        }
        return result;
    }

    private boolean canCallApi() {
        if (apiKey == null || apiKey.isBlank()) {
            log.debug("GEMINI_API_KEY가 설정되지 않아 번역 없이 원문을 표시합니다.");
            return false;
        }
        return System.currentTimeMillis() >= failedUntil;
    }

    private void callGemini(List<String> texts, String lang) {
        List<String> remaining = texts;
        boolean anyResponse = false;
        for (String model : models) {
            List<String> failed = tryModel(model.trim(), remaining, lang);
            if (failed.size() < remaining.size()) {
                anyResponse = true;
            }
            remaining = failed;
            if (remaining.isEmpty()) {
                break;
            }
        }

        if (!remaining.isEmpty()) {
            if (anyResponse) {
                // 모델은 응답했지만 번역이 필요 없다고 본 글(고유명사 등)은 원문을 캐시해서 다시 묻지 않는다.
                for (String text : remaining) {
                    cache.put(cacheKey(lang, text), text);
                }
            } else {
                failedUntil = System.currentTimeMillis() + FAIL_COOLDOWN_MS;
            }
        }
        if (remaining.size() < texts.size()) {
            saveCache();
        }
    }

    /** 이 모델로 번역하지 못한(호출 실패/원문 그대로 반환) 글의 목록을 돌려준다. */
    private List<String> tryModel(String model, List<String> texts, String lang) {
        try {
            String languageName = languageName(lang);
            String prompt = "You are a translation engine for a Japanese ryokan (inn) website. "
                    + "The input is a JSON array of strings. Each string may be written in Korean, English or Japanese. "
                    + "Translate EVERY string into " + languageName + ". "
                    + "Only a string that is already written in " + languageName + " may be returned unchanged. "
                    + "Keep proper nouns, numbers, prices, dates, e-mail addresses, URLs, line breaks and HTML tags as they are. "
                    + "Return ONLY a JSON array of strings with exactly " + texts.size()
                    + " elements, in the same order, with no explanation.\n\n"
                    + objectMapper.writeValueAsString(texts);

            Map<String, Object> body = Map.of(
                    "contents", new Object[]{Map.of("parts", new Object[]{Map.of("text", prompt)})},
                    "generationConfig", Map.of("responseMimeType", "application/json", "temperature", 0)
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint + "/" + model + ":generateContent"))
                    .timeout(Duration.ofSeconds(30))
                    .header("Content-Type", "application/json")
                    .header("x-goog-api-key", apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                log.warn("Gemini 번역 실패 (model={}, status={}): {}", model, response.statusCode(), abbreviate(response.body()));
                return texts;
            }

            JsonNode root = objectMapper.readTree(response.body());
            String json = root.path("candidates").path(0).path("content")
                    .path("parts").path(0).path("text").asString(null);
            JsonNode array = json == null ? null : objectMapper.readTree(json);
            if (array == null || !array.isArray() || array.size() != texts.size()) {
                log.warn("Gemini 응답 형식이 올바르지 않습니다 (model={}): {}", model, abbreviate(json));
                return texts;
            }

            List<String> failed = new ArrayList<>();
            for (int i = 0; i < texts.size(); i++) {
                String original = texts.get(i);
                String translated = array.get(i).asString(null);
                if (translated == null || translated.isBlank() || translated.trim().equals(original.trim())) {
                    failed.add(original);
                } else {
                    cache.put(cacheKey(lang, original), translated.trim());
                }
            }
            if (!failed.isEmpty()) {
                log.info("모델 {}이(가) {}건을 번역하지 않고 그대로 돌려줘 다음 모델로 재시도합니다.", model, failed.size());
            }
            return failed;
        } catch (Exception e) {
            log.warn("Gemini 번역 중 오류 발생 (model={})", model, e);
            return texts;
        }
    }

    private static String abbreviate(String s) {
        return s == null ? null : (s.length() > 300 ? s.substring(0, 300) + "..." : s);
    }

    /** 이미 목표 언어이거나 번역 대상이 아닌 글(숫자, 고유명사 한자만 등)이면 false. */
    static boolean needsTranslation(String text, String lang) {
        if (text == null || text.isBlank()) {
            return false;
        }
        boolean hangul = false, kana = false, han = false, latin = false;
        for (int i = 0; i < text.length(); ) {
            int cp = text.codePointAt(i);
            i += Character.charCount(cp);
            Character.UnicodeScript script = Character.UnicodeScript.of(cp);
            switch (script) {
                case HANGUL -> hangul = true;
                case HIRAGANA, KATAKANA -> kana = true;
                case HAN -> han = true;
                case LATIN -> latin = true;
                default -> { }
            }
        }
        if (!hangul && !kana && !han && !latin) {
            return false; // 숫자/기호뿐
        }
        if (!hangul && !kana && !latin) {
            return !"ja".equals(lang); // 한자뿐인 글은 일본어 화면에서만 그대로 두고, 한국어/영어 화면에서는 번역한다
        }
        return switch (lang) {
            case "ko" -> !(hangul && !kana);
            case "ja" -> !(kana && !hangul);
            default -> hangul || kana || han;
        };
    }

    private static String cacheKey(String lang, String text) {
        return lang + "|" + text;
    }

    private static String languageOf(Locale locale) {
        String lang = locale == null ? "ko" : locale.getLanguage();
        return switch (lang) {
            case "en", "ja" -> lang;
            default -> "ko";
        };
    }

    private static String languageName(String lang) {
        return switch (lang) {
            case "en" -> "English";
            case "ja" -> "Japanese";
            default -> "Korean";
        };
    }
}
