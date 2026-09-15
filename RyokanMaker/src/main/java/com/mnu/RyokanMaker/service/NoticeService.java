package com.mnu.RyokanMaker.service;

import com.mnu.RyokanMaker.dto.NoticeDto;
import com.mnu.RyokanMaker.mapper.NoticeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NoticeService {

    @Autowired
    private NoticeMapper noticeMapper;

    public List<NoticeDto> list() {
        return noticeMapper.selectList();
    }

    public NoticeDto select(int noticeIdx) {
        return noticeMapper.selectByIdx(noticeIdx);
    }
}
