package com.mnu.ryokanmaker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 객실별 매출 비중 (도넛 차트) 한 조각. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomShareDto {

    private String roomName;
    private Integer amount;
    private Integer percent;
}
