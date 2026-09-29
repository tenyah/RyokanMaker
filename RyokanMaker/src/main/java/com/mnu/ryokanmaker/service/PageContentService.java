package com.mnu.ryokanmaker.service;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mnu.ryokanmaker.domain.PageContentDto;
import com.mnu.ryokanmaker.mapper.PageContentMapper;

/**
 * 관리자가 수정하는 손님 화면 문구(PAGE_CONTENT). 값이 없는 키는 화면에서 messages 기본 문구로 대체한다.
 */
@Service
public class PageContentService {

    private static final Logger log = LoggerFactory.getLogger(PageContentService.class);

    /** /r/{adminId} 밖(관리자 패널 등)에서 부득이하게 필요할 때만 쓰는 기본값. */
    public static final int SITE_ADMIN_IDX = 1;

    private static final int MAX_BYTES = 1000;
    private static final long CACHE_MILLIS = 20_000L;

    @Autowired
    private PageContentMapper pageContentMapper;

    // 료칸(adminIdx)마다 별도로 캐시한다 - 단일 캐시였으면 두 번째 료칸이 첫 번째 료칸 문구를 보게 됨
    private final Map<Integer, Map<String, String>> siteCache = new ConcurrentHashMap<>();
    private final Map<Integer, Long> siteLoadedAt = new ConcurrentHashMap<>();

    /**
     * 키 → 문구 맵. 테이블이 없거나 DB가 불안정해도 화면이 죽지 않도록 빈 맵을 돌려주고,
     * 그러면 화면은 기본 문구로 표시된다.
     */
    public Map<String, String> getMap(Integer adminIdx) {
        Map<String, String> map = new HashMap<>();
        try {
            for (PageContentDto row : pageContentMapper.selectByAdmin(adminIdx)) {
                if (row.getContentText() != null && !row.getContentText().isBlank()) {
                    map.put(row.getPageKey(), row.getContentText());
                }
            }
        } catch (Exception e) {
            log.warn("페이지 문구 조회 실패 - 기본 문구로 표시합니다.", e);
        }
        return map;
    }

    /** 손님 화면용. 료칸(adminIdx)별로 매 요소마다 DB를 치지 않도록 짧게 캐시하고, 저장 시 바로 비운다. */
    public Map<String, String> getSiteMap(Integer adminIdx) {
        long now = System.currentTimeMillis();
        Long loadedAt = siteLoadedAt.get(adminIdx);
        if (loadedAt == null || now - loadedAt > CACHE_MILLIS) {
            siteCache.put(adminIdx, getMap(adminIdx));
            siteLoadedAt.put(adminIdx, now);
        }
        return siteCache.getOrDefault(adminIdx, Collections.emptyMap());
    }

    /**
     * 한 그룹의 문구를 저장한다. 폼 필드명은 pc_키. 값이 비어 있으면 행을 지워 기본 문구로 되돌리고,
     * 폼에 없는 키는 건드리지 않는다.
     */
    @Transactional
    public void saveGroup(Integer adminIdx, String group, Map<String, String> params) {
        try {
            for (PageTextDefs.Def def : PageTextDefs.ofGroup(group)) {
                String value = params.get("pc_" + def.key());
                if (value == null) {
                    continue;
                }
                value = value.replace("\r\n", "\n").strip();
                if (value.isEmpty()) {
                    pageContentMapper.delete(adminIdx, def.key());
                } else {
                    pageContentMapper.upsert(adminIdx, def.key(), truncateUtf8(value, MAX_BYTES));
                }
            }
        } finally {
            siteLoadedAt.remove(adminIdx);
        }
    }

    /** DB 컬럼이 VARCHAR2(n BYTE)라 글자 수가 아니라 UTF-8 바이트 수로 잘라야 한다 (한글·일본어는 글자당 3바이트). */
    public static String truncateUtf8(String value, int maxBytes) {
        if (value == null || value.getBytes(StandardCharsets.UTF_8).length <= maxBytes) {
            return value;
        }
        StringBuilder sb = new StringBuilder();
        int bytes = 0;
        for (int i = 0; i < value.length(); ) {
            int cp = value.codePointAt(i);
            int len = new String(Character.toChars(cp)).getBytes(StandardCharsets.UTF_8).length;
            if (bytes + len > maxBytes) {
                break;
            }
            sb.appendCodePoint(cp);
            bytes += len;
            i += Character.charCount(cp);
        }
        return sb.toString();
    }
}
