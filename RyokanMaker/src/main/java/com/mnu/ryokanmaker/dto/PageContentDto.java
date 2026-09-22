package com.mnu.ryokanmaker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageContentDto {
    private Integer adminIdx;
    private String pageKey;
    private String contentText;
}
