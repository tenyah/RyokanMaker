package com.mnu.ryokanmaker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 플랜 선택 화면(planSelect.html)에서 사용하는 플랜 정보 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlanDto {

    private String code;           // 플랜 코드 (예: ROOM_MEAL)
    private String name;           // 화면에 보여줄 플랜명
    private String imageUrl;       // 카드 썸네일 이미지 경로
    private boolean includesRoom;
    private boolean includesMeal;
    private boolean includesOnsen;
    private String description;
    private int priceFrom;         // 1박 2인 기준 시작 요금
}
