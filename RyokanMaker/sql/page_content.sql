-- 관리자가 수정하는 페이지별 안내 문구 (교통안내 화면의 소개문/지도 라벨/도보·버스·택시 카드 등)
-- 한국어 원문 한 벌만 저장하고, EN/JA는 화면에서 Gemini로 자동 번역한다.
-- 행이 없거나 CONTENT_TEXT가 비어 있으면 messages*.properties의 기본 문구가 표시된다.

CREATE TABLE PAGE_CONTENT (
    ADMIN_IDX     NUMBER         NOT NULL,
    PAGE_KEY      VARCHAR2(50)   NOT NULL,
    CONTENT_TEXT  VARCHAR2(1000),
    CONSTRAINT PK_PAGE_CONTENT PRIMARY KEY (ADMIN_IDX, PAGE_KEY),
    CONSTRAINT FK_ADMIN_TO_PAGE_CONTENT FOREIGN KEY (ADMIN_IDX) REFERENCES ADMIN (ADMIN_IDX)
);
