package com.mnu.ryokanmaker.dto;

import java.util.List;

/** 온천 시간대 표의 한 행(row) : 탕 이름 + 시간대별 예약 가능 여부 */
public class BathAvailabilityDto {

    private Long onsenIdx;
    private String bathName;
    private List<SlotDto> slots;

    public BathAvailabilityDto() {
    }

    public BathAvailabilityDto(Long onsenIdx, String bathName, List<SlotDto> slots) {
        this.onsenIdx = onsenIdx;
        this.bathName = bathName;
        this.slots = slots;
    }

    public Long getOnsenIdx() { return onsenIdx; }
    public void setOnsenIdx(Long onsenIdx) { this.onsenIdx = onsenIdx; }

    public String getBathName() { return bathName; }
    public void setBathName(String bathName) { this.bathName = bathName; }

    public List<SlotDto> getSlots() { return slots; }
    public void setSlots(List<SlotDto> slots) { this.slots = slots; }
}
