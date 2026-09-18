package com.mnu.ryokanmaker.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.mnu.ryokanmaker.dto.NoticeDto;

@Mapper
public interface NoticeMapper {

    /** 공지사항 전체 목록 (최신순) */
    List<NoticeDto> selectList();

    /** 공지사항 단건 조회 */
    NoticeDto selectByIdx(int noticeIdx);

    int insertNotice(NoticeDto noticeDto);

    List<NoticeDto> selectNoticesByAdmin(@Param("adminIdx") Integer adminIdx);

    int updateNotice(NoticeDto noticeDto);

    int deleteNotice(@Param("noticeIdx") Integer noticeIdx, @Param("adminIdx") Integer adminIdx);
}
