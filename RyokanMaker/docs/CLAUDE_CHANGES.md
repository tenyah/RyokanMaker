# Claude 작업 기록

Claude Code가 이 프로젝트에서 수정한 내용을 시간순으로 기록하는 파일입니다.

---

## 2026-09-16

- 이 기록 파일 생성 (`docs/CLAUDE_CHANGES.md`)

## 2026-09-19

- 객실/온천/식사/시설 소개 페이지(`rooms.html`, `onsen.html`, `dining.html`, `facility.html`) 레이아웃을
  gajoen.jp 샘플 참고하여 재배치: 카드 그리드 → 세로 목록으로 변경, 사진을 가운데 캐러셀(화살표/점 네비게이션,
  자동 슬라이드 없음)로 배치하고 그 아래에 텍스트 정보를 표시.
  - 객실: room_name → room_level → room_info → room_people → room_memo (+ 가격/예약 버튼 유지)
  - 온천: onsen_name → onsen_hour → onsen_info → onsen_memo
  - 식사: course_name → course_info → course_memo
  - 시설: facility_name → facility_info → facility_memo
- 공통 캐러셀 스크립트를 `src/main/resources/static/js/carousel.js`로 분리해 4개 페이지에서 공용으로 사용
  (기존 index.html의 인라인 캐러셀 로직과 동일한 동작).
- `common.css`에 `.detail-list` / `.detail-item` / `.detail-thumb` / `.detail-body` 스타일 추가,
  기존 `.room-grid/.room-card`, `.onsen-grid/.onsen-card`, `.facility-grid/.facility-card`는 다른 곳에서
  쓰이지 않아 제거함. `.course-grid/.course-card`는 `reservation.html`에서 재사용 중이라 그대로 유지.
- `FacilityDto`에 캐러셀용 `getImageUrls()` 메서드 추가 (기존엔 썸네일 1장만 뽑는 `getThumbnailUrl()`만 있었음).
- 객실 상세 캐러셀(`.detail-thumb`) 크기를 640×420 → 1024×580으로 확대. 업로드된 실제 사진 중 가장 좁은 폭이
  1024px라 이게 일반 모니터 기준 업스케일(화질 저하) 없이 키울 수 있는 최대치.
- 인덱스(`/`) 히어로 섹션을 고정 그라데이션 배경 → 캐러셀 배경으로 변경.
  - 데이터 출처: `ADMIN.RYOKAN_IMAGE` (관리자가 인덱스 화면에서 등록하는 "메인화면 슬라이드 이미지", 최대 10장).
    `AdminService.getRyokanInfo(adminIdx)` 신규 추가 → `ReservationController#mainIndex`에서
    `heroImages` 모델 속성으로 전달.
  - 이미지가 없으면(관리자 미등록 상태) 기존 `--gold-deep → --ink` 그라데이션이 그대로 보이도록 대체 처리.
  - 사진 위 텍스트 가독성을 위해 반투명 골드~잉크 오버레이(`.hero-overlay`)를 얹음. 화살표/점 네비게이션은
    기존 4개 소개 페이지와 동일하게 수동 전환만 지원(자동 슬라이드 없음).
- 공통 헤더/푸터(`include/header.html`, `include/footer.html`)의 "清流庵 / OTARU FURUKAWA" 텍스트 로고를
  `ADMIN.RYOKAN_LOGO` 이미지로, 푸터의 주소/전화 placeholder를 `ADMIN.ADMIN_LOC` / `ADMIN.RYOKAN_TEL`로
  교체하고 `ADMIN.ADMIN_MAIL`도 추가로 표시.
  - 로그인 여부와 무관하게 모든 방문자에게 보여야 해서, 세션 기반 `admin`과는 별도로
    `GlobalModelAdvice`에 `siteInfo` 전역 모델 속성을 추가(`AdminService.getRyokanInfo(1)`로 DB 조회,
    실패 시 null). 로고/주소/전화 미등록 시에는 기존 텍스트 로고·placeholder 문구로 자연스럽게 대체.
- 인덱스 히어로의 `<div class="eyebrow">OTARU FURUKAWA</div>`를 제거하고, `<h1>`을 `siteInfo.ryokanName`
  (미등록 시 '清流庵' 대체)으로 교체. 이제 쓰지 않는 `.hero .eyebrow` CSS도 함께 삭제.
- **DB에서 와야 하는데 하드코딩된 부분 전수 조사 후 수정** (요청받은 항목만: 관리자 화면 계정/사이드바 표시,
  `<title>` 태그, 죽은 파일 — 푸터 카피라이트 "© 2026 Ryokan Maker"는 사용자가 직접 수정한 값이라 손대지 않음):
  - 관리자 화면 6개(`admin_info_register`, `admin_inquiry`, `admin_requests`, `admin_reservation`,
    `plan_sales`, `room_status`)에 토씨 하나 안 틀리고 반복되던 하드코딩을 `admin` 세션 데이터로 교체:
    상단 로고/브랜드(`.site-header .logo`) → `admin.logoUrl`/`admin.ryokanName`(+고정 "ADMIN" 라벨),
    계정 드롭다운 이니셜/이름/ID(`account-trigger`, `avatar-sm`, `.name`, `.sub`) → `admin.adminName`
    첫 글자 / `admin.adminName` / `admin.adminId`(전부 "清流庵 관리자" · "ADMIN_ID" 리터럴이었음),
    사이드바 "관리 중인 여관" 카드(`.tenant-card`) → `admin.ryokanName`,
    사이드바 하단 프로필(`.admin-profile`) → `admin.adminName` / `admin.adminMail`(기존엔 심지어
    `admin@seiryuan.jp`라는 가짜 이메일이 6개 파일에 하드코딩돼 있었음).
  - 로그인 전 화면인 `admin_login.html`, `admin_pwreset.html`의 `.auth-logo`도 같은 문제라 같이 수정
    (세션 이전이라 `admin` 대신 `siteInfo` 사용, 로고 이미지 지원 위해 `.auth-logo img` CSS 추가).
  - 모든 값은 null-safe(`${admin?.X}` / `#strings.isEmpty`)로 처리해 비로그인·미등록 상태에서도
    기존 문구로 자연스럽게 대체됨(직접 curl로 무세션 상태 렌더링까지 확인).
  - 전체 26개 템플릿의 `<title>` 태그("... | 清流庵 오타루 후루카와" / "清流庵 관리자 - ...")를
    `siteInfo.ryokanName` 기반으로 통일(공개 페이지는 `siteInfo`, 관리자 페이지도 로그인 전에 열릴 수 있어
    동일하게 `siteInfo` 사용). 고정 "오타루 후루카와"/"OTARU FURUKAWA" 서브 브랜드 문구는 DB에 대응 필드가
    없어 제거.
  - 아무 컨트롤러도 참조하지 않던 죽은 파일 `admin/admin_info_register_b.html`(608줄, `admin_info_register.html`의
    구버전 백업으로 추정) 삭제.
- 헤더/푸터 로고 이미지 크기를 서로 다르던 44px(헤더)·40px(푸터)에서 64px로 통일 및 확대
  (`.site-header .logo img`, `.site-footer .brand img`, `max-width`도 200px → 240px).
- 인덱스 히어로 제목(`.hero h1`, DB의 `siteInfo.ryokanName` 출력)의 폰트가 헤더 로고 이미지 속
  붓글씨체와 달라 보인다는 지적에 따라, Google Fonts `Ma Shan Zheng`(붓글씨 느낌의 CJK 커시브 폰트)을
  `--brush` 변수로 추가해 `.hero h1`에 적용(폰트 미지원 문자는 기존 `--serif`로 자연 폴백).
  글자 크기도 44px → 56px로 키움(브러시 폰트가 동일 크기에서 상대적으로 가늘어 보여서).

## 2026-09-19 (프로젝트 전체 정리)

`.gitignore`에 `.DS_Store` 항목이 없어서 프로젝트 곳곳에 `.DS_Store`가 untracked로 떠돌던 것부터 시작해,
프로젝트 전체를 훑어 죽은 코드/미사용 리소스/보안 이슈를 찾아 보고(agent 활용) → 사용자가 고를 항목만 처리.

- `.DS_Store` 4개 삭제, `.gitignore`에 `.DS_Store` 항목 추가.
- **완전히 안 쓰이는 죽은 코드 삭제** (grep으로 재검증 후 삭제):
  - `PaymentMapper.java` + `PaymentMapper.xml` — 결제 흐름에서 실제로 안 쓰이는 MyBatis 매퍼 세트
    (`PaymentController`는 `PlanMapper`/`RoomMapper`/`RestaurantCourseMapper`를 직접 사용).
  - `PlanDto.java` — 자바독엔 "planSelect.html에서 사용"이라 적혀있었지만 실제로는 전부 `AdminPlanDto`를 씀.
    `AdminPlanDto.java`의 스테일해진 `{@link PlanDto}` 자바독 문구도 같이 정리.
  - 미사용 DTO 4개: `CourseDto.java`, `DayStatusDto.java`, `OnsenReservationDto.java`, `RestaurantReservationDto.java`.
- **미사용 CSS 클래스 3개 삭제**: `.auth-rule`, `.field-icon`, `.status-ask`
  (형제격인 `.status-ok`/`.status-no`는 쓰이는데 이것만 빠져있었음 — 예전에 쓰다 빠진 변형으로 추정).
- **admin 6개 파일(admin_info_register/inquiry/requests/reservation, plan_sales, room_status)의
  헤더·사이드바 중복(~920줄)을 공통 Thymeleaf fragment로 통합**:
  - 새 파일 `admin/fragments/admin_shell.html`에 `admin_icons`(공통 아이콘 스프라이트),
    `admin_topbar(activePage)`, `admin_sidebar(activePage, showInfoSubmenu, showRequestsLink)` 3개 fragment 정의.
  - 각 페이지는 자기 화면 본문 전용 아이콘만 남기고, 나머지는 fragment 호출로 대체.
    `activePage`로 상단/사이드바 active 상태를, `showInfoSubmenu`로 정보등록 페이지 전용 인페이지 서브메뉴를,
    `showRequestsLink`로 계정신청 페이지에만 있던 "계정 신청" 네비 항목을 각각 제어해 기존 페이지별 동작을
    그대로 재현(문의 관리 미답변 배지가 문의관리/계정신청 페이지 자신에게는 안 뜨던 것까지 동일하게 유지).
  - 곁다리로 발견한 버그 하나도 같이 고침: `admin_requests.html`의 "사이트 보기" 버튼이 다른 5개 페이지와
    달리 `<span>`(링크 없음)으로 되어 있어 클릭이 안 됐음 — fragment로 통합하면서 자연스럽게 정상 동작하는
    `<a>` 버전으로 통일됨.
  - 6개 파일 합계 717줄 삭제, 32줄만 추가(fragment 파일 177줄은 별도) — 실제 서버에서 4개 페이지
    (room_status/plan_sales/admin_requests + 로그인 필요한 나머지는 컴파일·구조 검증)를 직접 렌더링해
    active 상태·계정 정보·"계정 신청" 노출 여부까지 확인.
- `application.properties`의 평문 DB 비밀번호는 **의도적으로 그대로 둠** — 팀원이 별도로 처리 예정.

## 2026-09-21 (관리자 대시보드 · 매출현황 실 구현)

먼저 샘플 목업(스크래치패드, git 밖)으로 화면 구성을 여러 차례 다듬은 뒤, 사용자가 "이대로 프로젝트 작업해줘"라고
확정한 시점에 실제 서비스/화면으로 옮겼다. 대시보드/매출현황(월매출조회/일자별 매출조회) 3개 화면 전부 신규.

- **DB 스키마 재검토 후 목업의 일부 내용을 실 데이터에 맞게 조정** (DB에 없는 내용은 만들지 않는다는 기존
  원칙에 따름):
  - RESERVATION/ROOM_RESERVATION에는 결제상태로 `결제완료`/`결제대기`만 존재하고 취소·환불 개념 자체가
    없음(취소 기능 미구현) → 일자별 매출조회의 "취소·환불 내역"을 "결제대기 내역"으로 대체.
  - 체크인/체크아웃 목록의 VIP·얼리체크인·레이트체크아웃 태그는 대응 컬럼이 없어 제외. 대신 실제로 계산
    가능한 "연박"(체크인·체크아웃 날짜 차이)과 "요청사항"(RESV_REQUEST 비어있지 않음, 실제 텍스트 노출)만 사용.
  - RESERVATION에는 시각 정보가 없어(RESV_DAY는 날짜만) 거래 내역의 "결제일시" 컬럼을 "체크인일"로 변경 —
    이 프로젝트 전체가 체크인 날짜 기준 집계라는 원칙과도 일치.
  - 결제수단(카드/계좌이체 등)은 Toss가 반환하는 실제 값을 그대로 집계해 존재하는 수단만큼만 타일을
    동적으로 표시(카드/계좌이체 두 개로 미리 못박지 않음).
- **신규 매출 집계 쿼리**: `RoomReservationMapper.findByAdminAndCheckInRange`(관리자+체크인 날짜 범위,
  집계용) 추가, 신규 `RevenueMapper`(고객명/객실명/플랜명 조인 — 거래 목록·오늘 체크인/체크아웃 목록용) 추가.
  모두 체크인 날짜 기준이며 매출 판정은 `RESV_PAY_STATUS='결제완료'`만 인정.
- **신규 서비스**: `RevenueService`(월 요약/캘린더/요일별 분포/월별 추이/객실별 비중/플랜별 정산/거래 목록,
  OCC·ADR 계산 포함), `DashboardService`(오늘 KPI, 오늘 체크인/체크아웃 목록, 도착 희망시간 vs 현재시각
  비교로 완료/예정 판정).
- **신규 컨트롤러**: `AdminDashboardController`(`/Admin/dashboard`), `AdminRevenueController`
  (`/Admin/revenue_monthly?ym=yyyy-MM`, `/Admin/revenue_daily?period=today|week|month|lastmonth|custom`).
  월매출조회는 이전/다음 달 네비게이션(현재 달 이후로는 못 감), 일자별 매출조회는 기간 필터가 실제로
  서버 쿼리를 바꾸는 형태로 동작(목업 단계의 "클릭해도 안 바뀜" 한계를 해소).
- **신규 DTO 9개**: `RevenueTxnDto`, `RevenueDayDto`, `RevenueMonthSummaryDto`, `MonthlyTrendPointDto`,
  `RoomShareDto`, `PlanSettlementDto`, `CheckInOutRowDto`, `DashboardStatsDto`, `PaymentMethodStatDto`.
- **신규 템플릿 3개**: `admin/dashboard.html`, `admin/revenue_monthly.html`, `admin/revenue_daily.html`.
  대시보드의 "매출 및 예약 트렌드" 차트는 매출현황(월매출조회) 페이지와 동일한 Chart.js 코드를 그대로 재사용.
  Chart.js는 jsdelivr CDN, 축 단위(만/万/K)·범례("올해"/"This year"/"今年")는 화면 언어(`#locale.language`)에
  따라 분기.
- **`admin_shell.html` 공통 프래그먼트 확장**: 그동안 사이드바에 아예 없었고 상단바 링크도 `href="#"`로
  죽어있던 "대시보드"를 실제 라우트로 연결. "매출현황"을 상단바·사이드바에 신규 추가하고, 기존
  "정보등록"의 인페이지 서브메뉴(`showInfoSubmenu`)와 같은 패턴으로 `showRevenueSubmenu` 파라미터를 추가해
  월매출조회/일자별 매출조회 서브메뉴 표시. 프래그먼트 시그니처가 바뀌어 이 프래그먼트를 호출하던 기존
  6개 관리자 화면 전부에 `showRevenueSubmenu=false` 인자를 추가.
- `common.css`에 캘린더(`.rev-cal`)·월 네비게이터(`.month-nav`)·거래 표(`.txn-table`)·기간 필터
  (`.period-filter`)·오늘 체크인/체크아웃 표(`.ops-table`) 등 새 화면 전용 스타일 추가. 캘린더 주말(금·토)
  칸은 배경색만 쓰고 진한 테두리(box-shadow)는 넣지 않음(사용자 피드백 — "정신사납다").
- `messages*.properties`(KO/EN/JA) 3개 파일에 새 화면 라벨 60여 개 추가, 기존 `resv.status.*`
  (결제완료/결제대기 번역)와 `@tr.t()`/`@tr.label()` 헬퍼를 그대로 재사용해 다른 관리자 화면과 동일하게
  전체 다국어 적용.
- 로그인 가능한 관리자 계정 정보가 없어 실제 로그인 렌더링 확인은 못 했고(사용자가 "코드 검토로 대체"
  선택), 대신 `mvnw compile` 통과 + `curl`로 3개 라우트의 로그인 리다이렉트(302) 확인 +
  Thymeleaf 소스(`TemporalObjects`/`TemporalFormattingUtils`)까지 뒤져 `#temporals.format(YearMonth, ...)`
  동작을 직접 검증하는 등 코드 리뷰로 꼼꼼히 대체 검증. 리뷰 중 실제로 메시지 키 오용(체크인 완료 건수에
  엉뚱한 "체크인 예정 없음" 문구를 이어붙이던 버그), 누락된 CSS 클래스(`.ops-table`/`.status-pill2`),
  3-인자 `#numbers.formatDecimal` EL 표현식의 불확실성(서버 계산으로 대체) 등을 찾아 수정.
