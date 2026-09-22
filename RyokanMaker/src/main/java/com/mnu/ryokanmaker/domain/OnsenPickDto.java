package com.mnu.ryokanmaker.domain;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 사용자가 고른 "어느 날, 어느 온천, 몇 시" 한 건 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OnsenPickDto {

    private LocalDate date;
    private Integer onsenIdx;
    private String timeSlot;   // "15:00"
}
