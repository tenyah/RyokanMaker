package com.mnu.RyokanMaker.dto;

/** 식사(플랜) 선택 영역의 코스 카드 하나 */
public class CourseDto {

    private String name;
    private String imageUrl;
    private String description;
    private boolean available;   // false면 "[코스 상세 설명 준비 중입니다]" 표시 + 버튼 비활성화

    public CourseDto() {
    }

    public CourseDto(String name, String imageUrl, String description, boolean available) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.description = description;
        this.available = available;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
}
