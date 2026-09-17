package com.mnu.ryokanmaker.domain;

import lombok.Data;

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
    private String onsenImage;   // ONSEN_IMAGE (CLOB) - 이미지 JSON 배열(base64) 저장
    private String onsenInfo;
    private Integer adminIdx;
    private String onsenSaleYn;  // ONSEN_SALE_YN, 기본값 'Y'
    private String onsenMemo;
    private String onsenHour;    // 예: "15:00–21:00"

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

    /** 화면(수정 모드)에서 시간 input에 다시 채워 넣기 위해 ONSEN_HOUR를 시작/종료로 분리 */
    public String getOnsenStartTime() {
        if (onsenHour == null || !onsenHour.contains(HOUR_DELIMITER)) return "";
        return onsenHour.split(HOUR_DELIMITER, 2)[0];
    }

    public String getOnsenEndTime() {
        if (onsenHour == null || !onsenHour.contains(HOUR_DELIMITER)) return "";
        String[] parts = onsenHour.split(HOUR_DELIMITER, 2);
        return parts.length > 1 ? parts[1] : "";
    }
}
