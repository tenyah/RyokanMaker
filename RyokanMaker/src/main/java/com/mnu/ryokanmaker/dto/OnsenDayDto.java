package com.mnu.ryokanmaker.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 숙박 기간 중 하루(date)에 대한 온천별 시간대 선택표 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OnsenDayDto {

    private LocalDate date;
    private List<BathAvailabilityDto> baths;
}
