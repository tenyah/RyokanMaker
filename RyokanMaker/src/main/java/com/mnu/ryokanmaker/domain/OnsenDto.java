package com.mnu.ryokanmaker.domain;

import java.util.List;

import lombok.Data;

import com.mnu.ryokanmaker.util.ImageJsonUtil;

/**
 * ONSEN 테이블 매핑 DTO (온천 마스터 정보)
 * PK : onsenIdx (IDENTITY, 자동 채번) / FK : adminIdx -> ADMIN.ADMIN_IDX
 * 이용 시작/종료 시간은 컬럼이 따로 없고 ONSEN_HOUR 하나(문자열)에 "15:00–21:00" 형태로 합쳐서 저장.
 */
@Data
public class OnsenDto {

    private static final String HOUR_DELIMITER = "–"; // en dash

    private Integer onsenIdx;
    private String onsenName;
    private String onsenImage;   // ONSEN_IMAGE (CLOB) - 이미지 경로 JSON 배열 문자열 저장
    private String onsenInfo;
    private Integer adminIdx;
    private String onsenSaleYn;  // ONSEN_SALE_YN, 기본값 'Y'
    private String onsenMemo;
    private String onsenHour;    // 예: "15:00–21:00"
    private Integer onsenPrice;  // ONSEN_PRICE, 관리자가 입력하는 온천 가격

    /** 목록 화면 썸네일용. onsenImage(경로 JSON 배열)에서 첫 번째 이미지 경로만 뽑는다. */
    public String getThumbnailUrl() {
        return ImageJsonUtil.firstPath(onsenImage);
    }

    /** 메인 페이지 캐러셀용. onsenImage(경로 JSON 배열)의 전체 이미지 경로 목록. */
    public List<String> getImageUrls() {
        return ImageJsonUtil.parsePaths(onsenImage);
    }

    /** ONSEN_HOUR를 "시작–종료"로 합쳐서 세팅 (둘 다 비어있으면 null) */
    public void setOnsenHourFromRange(String startTime, String endTime) {
        boolean hasStart = startTime != null && !startTime.isEmpty();
        boolean hasEnd = endTime != null && !endTime.isEmpty();
        if (!hasStart && !hasEnd) {
            this.onsenHour = null;
            return;
        }
        this.onsenHour = (hasStart ? startTime : "") + HOUR_DELIMITER + (hasEnd ? endTime : "");
    }

    // 저장은 항상 엔대시(–)로 하지만, DB에 직접 넣은 데이터는 ~ 나 - 로 되어 있을 수 있어 읽을 때는 모두 허용
    private static final java.util.regex.Pattern HOUR_SPLIT = java.util.regex.Pattern.compile("[–~〜～-]");

    /** ONSEN_HOUR를 [시작, 종료]로 분리. 구분자가 없으면 null. */
    private String[] splitHour() {
        if (onsenHour == null) return null;
        String[] parts = HOUR_SPLIT.split(onsenHour, 2);
        return parts.length == 2 ? parts : null;
    }

    /** 화면(수정 모드)에서 시간 input에 다시 채워 넣기 위해 ONSEN_HOUR를 시작/종료로 분리 */
    public String getOnsenStartTime() {
        String[] parts = splitHour();
        return parts == null ? "" : parts[0].strip();
    }

    public String getOnsenEndTime() {
        String[] parts = splitHour();
        return parts == null ? "" : parts[1].strip();
    }
}
