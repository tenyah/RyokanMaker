package com.mnu.ryokanmaker.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 관리자가 수정할 수 있는 손님 화면 문구 목록. 관리자 입력 화면과 손님 화면이 이 목록을 함께 쓴다.
 * 새 문구를 추가하려면 여기에 한 줄 + messages 3개 파일(기본 문구, 관리자 라벨)에 키만 추가하면 된다.
 *
 * @param group      관리자 화면의 묶음 (INDEX/ACCESS/PLANS/MEMBER/INQUIRY)
 * @param key        PAGE_CONTENT.PAGE_KEY
 * @param messageKey 값이 없을 때 쓰는 기본 문구(messages*.properties)
 * @param multiline  여러 줄 입력(textarea) 여부
 */
public final class PageTextDefs {

    public record Def(String group, String key, String messageKey, boolean multiline) {
        /** 관리자 화면 라벨용 messages 키 (adm.pt_소문자키) */
        public String labelKey() {
            return "adm.pt_" + key.toLowerCase();
        }
    }

    public static final List<Def> ALL = List.of(
            new Def("INDEX", "INDEX_TAGLINE", "index.tagline", false),
            new Def("INDEX", "INDEX_INTRO_TITLE", "index.intro_title", true),
            new Def("INDEX", "INDEX_INTRO_BODY", "index.intro_body", true),
            new Def("INDEX", "INDEX_ROOM_DESC", "index.room_desc", true),
            new Def("INDEX", "INDEX_ONSEN_DESC", "index.onsen_desc", true),
            new Def("INDEX", "INDEX_DINING_DESC", "index.dining_desc", true),

            new Def("ACCESS", "ACCESS_DESC", "access.desc", false),
            new Def("ACCESS", "ACCESS_STATION", "access.station", false),
            new Def("ACCESS", "ACCESS_BUSSTOP", "access.busstop", false),
            new Def("ACCESS", "ACCESS_BUSSTOP_SUB", "access.busstop_sub", false),
            new Def("ACCESS", "ACCESS_WALK_DESC", "access.walk_desc", true),
            new Def("ACCESS", "ACCESS_BUS_DESC", "access.bus_desc", true),
            new Def("ACCESS", "ACCESS_BUS_FARE", "access.bus_fare", false),
            new Def("ACCESS", "ACCESS_TAXI_DESC", "access.taxi_desc", true),
            new Def("ACCESS", "ACCESS_TAXI_FARE", "access.taxi_fare", false),

            new Def("MENU", "ROOMS_TITLE", "rooms.title", false),
            new Def("MENU", "ROOMS_DESC", "rooms.desc", true),
            new Def("MENU", "ONSEN_TITLE", "onsen.title", false),
            new Def("MENU", "ONSEN_DESC", "onsen.desc", true),
            new Def("MENU", "ONSEN_NOTE", "onsen.note", false),
            new Def("MENU", "DINING_TITLE", "dining.title", false),
            new Def("MENU", "DINING_DESC", "dining.desc", true),
            new Def("MENU", "DINING_NOTE", "dining.note", false),
            new Def("MENU", "FACILITY_TITLE", "facility.title", false),
            new Def("MENU", "FACILITY_DESC", "facility.desc", true),

            new Def("PLANS", "PLANS_HEADING", "plans.heading", false),
            new Def("PLANS", "PLANS_DESC", "plans.desc", true),

            new Def("MEMBER", "LOGIN_DESC", "login.desc", false),
            new Def("MEMBER", "SIGNUP_DESC", "signup.desc", false),
            new Def("MEMBER", "MYPAGE_DESC", "mypage.desc", false),

            new Def("INQUIRY", "INQUIRY_LIST_DESC", "inquiry.list_desc", false),
            new Def("INQUIRY", "INQUIRY_WRITE_DESC", "inquiry.write_desc", false));

    /** 그룹 코드 → 문구 목록 (관리자 화면 표시 순서 유지) */
    public static Map<String, List<Def>> byGroup() {
        Map<String, List<Def>> groups = new LinkedHashMap<>();
        for (Def def : ALL) {
            groups.computeIfAbsent(def.group(), g -> new java.util.ArrayList<>()).add(def);
        }
        return groups;
    }

    public static List<Def> ofGroup(String group) {
        return ALL.stream().filter(d -> d.group().equals(group)).toList();
    }

    public static Optional<Def> find(String key) {
        return ALL.stream().filter(d -> d.key().equals(key)).findFirst();
    }

    private PageTextDefs() {
    }
}
