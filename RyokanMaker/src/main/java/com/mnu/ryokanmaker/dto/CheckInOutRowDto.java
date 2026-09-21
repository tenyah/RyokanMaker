package com.mnu.ryokanmaker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 대시보드 "오늘 체크인 / 오늘 체크아웃" 목록 한 줄.
 * arrivalTime은 체크인 목록에서만 쓰고, nights(연박 여부 판단용)·hasRequest·resvPayStatus는 공통으로 쓴다.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckInOutRowDto {

    private String userNickname;
    private String roomName;
    private String planName;
    private String resvArrivalTime;
    private int nights;
    private boolean hasRequest;
    private String resvRequest;
    private String resvPayStatus;
    /** 체크인 목록에서만 의미 있음 : 희망 도착 시간이 이미 지났는지 (체크아웃은 시간 데이터가 없어 항상 false). */
    private boolean completed;
}
