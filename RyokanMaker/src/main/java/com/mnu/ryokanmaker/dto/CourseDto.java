package com.mnu.ryokanmaker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 식사(플랜) 선택 영역의 코스 카드 하나 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseDto {

    private String name;
    private String imageUrl;
    private String description;
    private boolean available;   // false면 "[코스 상세 설명 준비 중입니다]" 표시 + 버튼 비활성화
}
