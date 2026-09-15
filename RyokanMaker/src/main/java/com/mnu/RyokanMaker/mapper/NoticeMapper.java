package com.mnu.RyokanMaker.mapper;

import com.mnu.RyokanMaker.dto.NoticeDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface NoticeMapper {

    /** 공지사항 전체 목록 (최신순) */
    List<NoticeDto> selectList();

    /** 공지사항 단건 조회 */
    NoticeDto selectByIdx(int noticeIdx);
}
