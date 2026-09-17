package com.mnu.ryokanmaker.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.mnu.ryokanmaker.domain.NoticeDto;

@Mapper
public interface NoticeMapper {
	public int insertNotice(NoticeDto noticeDto);
	public List<NoticeDto> selectNoticesByAdmin(@Param("adminIdx") Integer adminIdx);
	public int updateNotice(NoticeDto noticeDto);
	public int deleteNotice(@Param("noticeIdx") Integer noticeIdx, @Param("adminIdx") Integer adminIdx);
}
