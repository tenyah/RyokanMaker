package com.mnu.ryokanmaker.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.mnu.ryokanmaker.domain.PageContentDto;

@Mapper
public interface PageContentMapper {

    List<PageContentDto> selectByAdmin(@Param("adminIdx") Integer adminIdx);

    /** 없으면 등록, 있으면 수정 */
    int upsert(@Param("adminIdx") Integer adminIdx,
               @Param("pageKey") String pageKey,
               @Param("contentText") String contentText);

    /** 기본 문구로 되돌리기 */
    int delete(@Param("adminIdx") Integer adminIdx, @Param("pageKey") String pageKey);
}
