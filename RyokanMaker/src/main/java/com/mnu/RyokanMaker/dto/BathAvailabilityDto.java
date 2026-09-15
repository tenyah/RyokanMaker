package com.mnu.RyokanMaker.dto;

import java.util.List;

/** 온천 시간대 표의 한 행(row) : 탕 이름 + 시간대별 예약 가능 여부 */
public class BathAvailabilityDto {

    private String bathName;
    private List<SlotDto> slots;

    public BathAvailabilityDto() {
    }

    public BathAvailabilityDto(String bathName, List<SlotDto> slots) {
        this.bathName = bathName;
        this.slots = slots;
    }

    public String getBathName() { return bathName; }
    public void setBathName(String bathName) { this.bathName = bathName; }

    public List<SlotDto> getSlots() { return slots; }
    public void setSlots(List<SlotDto> slots) { this.slots = slots; }
}
