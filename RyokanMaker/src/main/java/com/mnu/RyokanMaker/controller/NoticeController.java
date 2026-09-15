package com.mnu.RyokanMaker.controller;

import com.mnu.RyokanMaker.dto.NoticeDto;
import com.mnu.RyokanMaker.service.NoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 유저(비회원 포함)에게 보이는 공지사항 화면.
 * - 목록 : templates/notice/list.html
 * - 상세 : templates/notice/view.html
 */
@Controller
public class NoticeController {

    @Autowired
    private NoticeService noticeService;

    @GetMapping("/notice/list")
    public String list(Model model) {
        model.addAttribute("noticeList", noticeService.list());
        return "notice/list";
    }

    @GetMapping("/notice/view")
    public String view(@RequestParam int idx, Model model) {
        NoticeDto notice = noticeService.select(idx);
        model.addAttribute("notice", notice);
        return "notice/view";
    }
}
