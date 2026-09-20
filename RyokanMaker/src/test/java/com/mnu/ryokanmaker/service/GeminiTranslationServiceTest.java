package com.mnu.ryokanmaker.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;

class GeminiTranslationServiceTest {

    @Test
    void koreanTextNeedsTranslationOnlyForEnglishAndJapanese() {
        assertFalse(GeminiTranslationService.needsTranslation("객실 안내", "ko"));
        assertTrue(GeminiTranslationService.needsTranslation("객실 안내", "en"));
        assertTrue(GeminiTranslationService.needsTranslation("객실 안내", "ja"));
    }

    @Test
    void japaneseTextNeedsTranslationForKoreanAndEnglish() {
        assertTrue(GeminiTranslationService.needsTranslation("露天風呂の利用時間について", "ko"));
        assertTrue(GeminiTranslationService.needsTranslation("露天風呂の利用時間について", "en"));
        assertFalse(GeminiTranslationService.needsTranslation("露天風呂の利用時間について", "ja"));
    }

    @Test
    void englishTextNeedsTranslationForKoreanAndJapanese() {
        assertTrue(GeminiTranslationService.needsTranslation("Open-air bath hours", "ko"));
        assertTrue(GeminiTranslationService.needsTranslation("Open-air bath hours", "ja"));
        assertFalse(GeminiTranslationService.needsTranslation("Open-air bath hours", "en"));
    }

    @Test
    void hanOnlyTextIsKeptOnlyForJapanese() {
        assertFalse(GeminiTranslationService.needsTranslation("清流庵", "ja"));
        assertTrue(GeminiTranslationService.needsTranslation("清流庵", "en"));
        assertTrue(GeminiTranslationService.needsTranslation("清流庵", "ko"));
    }

    @Test
    void numbersAndBlankAreNeverTranslated() {
        for (String lang : List.of("ko", "en", "ja")) {
            assertFalse(GeminiTranslationService.needsTranslation("2026.09.20", lang));
            assertFalse(GeminiTranslationService.needsTranslation("₩300,000", lang));
            assertFalse(GeminiTranslationService.needsTranslation("  ", lang));
            assertFalse(GeminiTranslationService.needsTranslation(null, lang));
        }
    }

    @Test
    void withoutApiKeyTheOriginalTextIsReturned() {
        GeminiTranslationService service = new GeminiTranslationService();
        assertEquals("객실 안내", service.translate("객실 안내", Locale.ENGLISH));
        assertEquals(null, service.translate(null, Locale.JAPANESE));
    }
}
