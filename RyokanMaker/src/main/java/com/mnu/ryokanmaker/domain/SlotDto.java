package com.mnu.ryokanmaker.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 온천 시간대 표의 칸 하나 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SlotDto {

    private String time;       // "15:00"
    private boolean available;
}
