package com.mnu.ryokanmaker.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 온천 시간대 표의 한 행(row) : 탕 이름 + 시간대별 예약 가능 여부 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BathAvailabilityDto {

    private Integer onsenIdx;
    private String bathName;
    private List<SlotDto> slots;
}
