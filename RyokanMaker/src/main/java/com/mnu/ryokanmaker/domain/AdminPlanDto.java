package com.mnu.ryokanmaker.domain;

import lombok.Data;

import com.mnu.ryokanmaker.util.ImageJsonUtil;

/**
 * PLAN 테이블 매핑 DTO (숙박 플랜 마스터 정보, 관리자 등록/수정용)
 * PK : planIdx (IDENTITY, 자동 채번) / FK : adminIdx -> ADMIN.ADMIN_IDX
 * 사용자 예약 화면(플랜 선택)에서 실제로 가격이 노출되는 단위. 객실 가격(roomPrice)은
 * 플랜 기준가 대비 추가요금 계산에만 쓰이고, 사이트에는 플랜 가격만 표시된다.
 * 이름이 같은 {@link PlanDto}는 사용자 예약 화면(플랜 선택) 전용 DTO로 별개이니 혼동하지 말 것.
 */
@Data
public class AdminPlanDto {

    private Integer planIdx;
    private Integer adminIdx;
    private String planName;
    private String planImage;          // PLAN_IMAGE (CLOB) - 이미지 경로 JSON 배열 문자열 저장
    private String planIncludesMeal;   // PLAN_INCLUDES_MEAL, 기본값 'N'
    private String planIncludesOnsen;  // PLAN_INCLUDES_ONSEN, 기본값 'N'
    private String planInfo;
    private Integer planPrice;
    private String planSaleYn;         // PLAN_SALE_YN, 기본값 'Y'

    /** 목록 화면 썸네일용. planImage(경로 JSON 배열)에서 첫 번째 이미지 경로만 뽑는다. */
    public String getThumbnailUrl() {
        return ImageJsonUtil.firstPath(planImage);
    }
}
