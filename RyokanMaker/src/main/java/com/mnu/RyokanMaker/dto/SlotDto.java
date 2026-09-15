package com.mnu.RyokanMaker.dto;

/** 온천 시간대 표의 칸 하나 */
public class SlotDto {

    private String time;       // "15:00"
    private boolean available;

    public SlotDto() {
    }

    public SlotDto(String time, boolean available) {
        this.time = time;
        this.available = available;
    }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
}
