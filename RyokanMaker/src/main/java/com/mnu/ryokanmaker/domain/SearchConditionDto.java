package com.mnu.ryokanmaker.domain;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 검색 조건 바(체크인/체크아웃, 인원, 객실 수)에서 쓰는 값 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchConditionDto {

    private LocalDate checkIn;
    private LocalDate checkOut;
    private int adultCount;
    private int childCount;
    private int roomCount;
}
