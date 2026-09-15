package com.mnu.RyokanMaker.dto;

/** 플랜 선택 화면(planSelect.html)에서 사용하는 플랜 정보 */
public class PlanDto {

    private String code;           // 플랜 코드 (예: ROOM_MEAL)
    private String name;           // 화면에 보여줄 플랜명
    private String imageUrl;       // 카드 썸네일 이미지 경로
    private boolean includesRoom;
    private boolean includesMeal;
    private boolean includesOnsen;
    private String description;
    private int priceFrom;         // 1박 2인 기준 시작 요금

    public PlanDto() {
    }

    public PlanDto(String code, String name, String imageUrl,
                   boolean includesRoom, boolean includesMeal, boolean includesOnsen,
                   String description, int priceFrom) {
        this.code = code;
        this.name = name;
        this.imageUrl = imageUrl;
        this.includesRoom = includesRoom;
        this.includesMeal = includesMeal;
        this.includesOnsen = includesOnsen;
        this.description = description;
        this.priceFrom = priceFrom;
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public boolean isIncludesRoom() { return includesRoom; }
    public void setIncludesRoom(boolean includesRoom) { this.includesRoom = includesRoom; }

    public boolean isIncludesMeal() { return includesMeal; }
    public void setIncludesMeal(boolean includesMeal) { this.includesMeal = includesMeal; }

    public boolean isIncludesOnsen() { return includesOnsen; }
    public void setIncludesOnsen(boolean includesOnsen) { this.includesOnsen = includesOnsen; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getPriceFrom() { return priceFrom; }
    public void setPriceFrom(int priceFrom) { this.priceFrom = priceFrom; }
}
