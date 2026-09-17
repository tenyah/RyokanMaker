package com.mnu.ryokanmaker.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mnu.ryokanmaker.domain.NoticeDto;
import com.mnu.ryokanmaker.mapper.NoticeMapper;

@Service
public class NoticeService {

	@Autowired
	private NoticeMapper noticeMapper;

	public List<NoticeDto> getNoticeList(Integer adminIdx) {
		return noticeMapper.selectNoticesByAdmin(adminIdx);
	}

	/**
	 * noticeIdx가 없으면(0 또는 null) 신규 등록, 있으면 수정으로 처리 (upsert)
	 * 이미지/노출여부 없음 - 제목/내용만 있는 테이블
	 */
	public void saveNotice(NoticeDto noticeDto) {
		boolean isUpdate = noticeDto.getNoticeIdx() != null && noticeDto.getNoticeIdx() > 0;
		if (isUpdate) {
			noticeMapper.updateNotice(noticeDto);
		} else {
			noticeMapper.insertNotice(noticeDto);
		}
	}

	public void deleteNotice(Integer noticeIdx, Integer adminIdx) {
		noticeMapper.deleteNotice(noticeIdx, adminIdx);
	}
}
