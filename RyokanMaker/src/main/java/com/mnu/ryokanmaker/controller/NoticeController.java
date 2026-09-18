package com.mnu.ryokanmaker.controller;

import com.mnu.ryokanmaker.dto.NoticeDto;
import com.mnu.ryokanmaker.service.GeminiTranslationService;
import com.mnu.ryokanmaker.service.NoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 유저(비회원 포함)에게 보이는 공지사항 화면.
 * 목록 페이지는 따로 두지 않고, 메인 화면에서 최신 몇 건만 미리보기로 보여준 뒤
 * 각 항목을 상세(view)로 연결하는 방식으로 간다 (팀 논의 결과).
 * - 상세 : templates/notice/view.html
 */
@Controller
public class NoticeController {

    @Autowired
    private NoticeService noticeService;

    @Autowired
    private GeminiTranslationService translationService;

    @GetMapping("/notice/view")
    public String view(@RequestParam int idx, Model model) {
        NoticeDto notice = noticeService.select(idx);

        // 현재 언어(헤더의 KO/EN/JA 전환)가 한국어가 아니면 Gemini로 즉석 번역해서 보여줌
        notice.setNoticeTitle(translationService.translate(notice.getNoticeTitle(), LocaleContextHolder.getLocale()));
        notice.setNoticeContent(translationService.translate(notice.getNoticeContent(), LocaleContextHolder.getLocale()));

        model.addAttribute("notice", notice);
        return "notice/view";
    }
}
