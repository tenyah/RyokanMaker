# 작업 기록

## 관리자가 손님 화면 소개·설명 문구를 수정하는 기능 (PAGE_CONTENT) (2026-09-21)

**배경:** 페이지마다 소개·설명 문구가 `messages*.properties`(코드)에 박혀 있어 관리자가 고칠 수 없었음. 특히 교통안내는 버스 노선·요금이 바뀔 수 있는 내용인데 `ADMIN.RYOKAN_ACCESS` 한 줄(당시 33자)뿐이었음. 브랜치 `Choiyeongsu13` (Test에는 아직 미반영).

**DB (새 테이블 1개, 추가 컬럼 없음):** `PAGE_CONTENT(ADMIN_IDX, PAGE_KEY, CONTENT_TEXT)`, PK `(ADMIN_IDX, PAGE_KEY)`, FK → `ADMIN`. 문구를 열이 아니라 **행**으로 저장해서, 문구가 늘어도 DB는 그대로. `ADMIN`에 컬럼을 늘리는 방식은 문구마다 `ALTER TABLE`이 필요하고 로그인 정보 테이블이 비대해져서 채택하지 않음. `sql/page_content.sql`, ERDCloud에도 반영 완료(ERD는 `ADMIN.Ryokan_Access`도 100→1000으로 수정).

**구조:**
- `PageTextDefs`: 수정 가능한 문구 32개 목록(그룹/키/기본 messages 키/여러 줄 여부). 관리자 화면과 손님 화면이 이 목록을 함께 씀. **새 문구 추가 = 이 목록에 한 줄 + messages 3개 파일에 라벨/기본문구 키.**
- `PageContentService`: 조회(손님용은 20초 캐시, 저장 시 즉시 무효화), 그룹 단위 저장(빈 값이면 행 삭제 = 기본 문구로 복귀). 테이블이 없거나 DB 오류여도 예외 없이 기본 문구로 표시.
- `PageTextHelper`(`@pt`): 템플릿에서 `${@pt.t('KEY')}`. 저장된 값(한국어 원문)이 있으면 기존 Gemini 번역(`GeminiTranslationService`)으로 현재 언어로 보여주고, 없으면 messages 기본 문구. **한국어로만 입력하면 EN/JA는 자동 번역.**
- 관리자 정보등록 화면: 교통안내 섹션은 목록 반복 입력으로 교체, 새 "페이지 문구" 섹션(`#section-pagetext`) 추가, 사이드바 메뉴 링크 추가. 저장은 `POST /Admin/admin_access_save`(교통안내+한 줄 안내), `POST /Admin/admin_page_text_save`(그룹 코드).

**수정 가능한 문구 (32개):** 메인(첫 화면 한 줄, 소개 제목/본문, 객실·온천·식사 카드 설명) 6 / 교통안내(소개문, 지도 글자 3, 도보·버스·택시 안내와 요금) 9 / 객실·온천·식사·시설 페이지(제목 4, 소개문 4, 온천·식사 하단 안내 2) 10 / 플랜 선택(제목, 소개문) 2 / 로그인·회원가입·마이페이지 소개문 3 / 문의 목록·작성 소개문 2. 객실·온천·식사·시설 각 항목의 설명은 원래 관리자 화면에서 수정 가능했음. 메뉴·버튼·폼 항목명·푸터 문구·결제 화면 안내는 대상 아님(고정 라벨).

**정리한 것:** 어디에서도 링크되지 않던 옛 단독 화면 `/Admin/access_edit`와 `updateAccess`(서비스/매퍼/XML)는 삭제하고 주소는 정보등록의 교통안내 섹션으로 리다이렉트. 교통안내 화면의 도보/버스/택시 카드와 지도 그림 글자도 이제 DB 문구를 사용.

**글자 수 제한 (중요):** DB는 `NLS_LENGTH_SEMANTICS=BYTE`, 문자셋 `AL32UTF8`이라 `VARCHAR2(1000)`은 1000**바이트**. 한글·일본어는 글자당 3바이트라 약 333자가 한계이고 처음 33자 제한도 `100바이트÷3`에서 나온 값이었음. `ADMIN.RYOKAN_ACCESS`가 100→1000으로 늘어서 입력 제한을 **33자→300자**로 풀고, 페이지 문구 입력칸도 300자로 제한. 브라우저 제한을 우회해도 서버에서 UTF-8 1000바이트 이내로 자르도록 `PageContentService.truncateUtf8` 적용(400자 넘는 한글+일본어 저장 시 333자/999바이트로 잘려 오류 없이 저장됨을 확인).

**작업 중 발견/수정한 문제:**
- 메인 소개 제목의 `<br>`를 `th:utext`로 출력하려다 Thymeleaf가 `th:utext`에서 빈 호출을 막아 메인 화면이 오류남. 줄바꿈을 `\n` + CSS `white-space: pre-line`(`th:text`)으로 바꿔 해결(기본 문구도 `<br>` → `\n`).
- 메시지 파일 일괄 수정 중 `sed`가 `\n`을 진짜 줄바꿈으로 해석해 `index.intro_title` 줄이 3개 파일에서 둘로 쪼개졌던 것을 바로 복구(세 파일 줄 수 동일 확인).
- ERDCloud에서 `PAGE_CONTENT`의 PK가 `PAGE_KEY` 하나로 되어 있던 것을 복합키로 수정(료칸이 둘 이상이면 같은 키를 못 쓰는 문제였음). `CONTENT_TEXT` NOT NULL→NULL, `VARCHAR`→`VARCHAR2`도 실제 DB에 맞춤.

**검증:** `mvnw clean compile` 성공. 관리자 로그인 없이 세션에 관리자 객체를 넣은 임시 MockMvc 테스트(커밋 안 함)로 실제 DB에 대해: 관리자 화면 KO/EN/JA 200·입력칸 표시, 저장→손님 화면 반영→관리자 화면 재표시→비워서 저장 시 기본 문구 복귀(메인/교통안내/객실·온천·식사·시설 모두), 빈 소개문은 화면에 표시되지 않음, 긴 문구 저장 시 바이트 제한 동작. 시험 값은 모두 지워서 `PAGE_CONTENT`는 비어 있음. **관리자 화면을 사람이 직접 브라우저로 열어 눌러본 것은 아님(로그인 계정 없이 검증).**

**다음 할 일 / 주의:**
- `Test`에 합칠 때 팀원 DB에 `sql/page_content.sql` 실행 필요(각자 다른 DB를 쓰는 경우). 테이블이 없어도 앱은 죽지 않고 기본 문구만 표시됨.
- 아직 고정인 것: 메뉴·버튼·폼 항목명, 페이지 breadcrumb(`Guestroom / お部屋` 등), 푸터 저작권 문구, 메인 하단 공지 제목·관리자 신청 안내, 결제·예약 화면 안내. 필요하면 `PageTextDefs`에 추가하면 됨.
- 손님 화면은 관리자 1번(`SITE_ADMIN_IDX = 1`)의 문구를 보여줌(다른 컨트롤러의 `MAIN_ADMIN_IDX`와 같은 단일 료칸 가정).

---

## 사용자 화면 전체 다국어(KO/EN/JA) 적용 (2026-09-21)

**목표:** 외국인 이용을 고려해, 어떤 언어(한국어/일본어/영어)로 쓴 내용이든 보는 사람이 고른 언어로 보이게 한다. 브랜치 `i18n-all`.

**구조 (문구 종류별로 두 층):**
1. **고정 UI 문구**(라벨/버튼/안내문/오류·확인창): `messages.properties`(KO) / `messages_en` / `messages_ja`에 키를 넣고 템플릿을 `#{...}`로 교체. 번역은 직접 작성해서 API 없이 즉시 표시. 키 약 220개 추가(총 242개, 세 언어 파일의 키가 동일한지 검사함). `<html lang>`도 현재 언어로 설정.
2. **DB에서 온 자유 텍스트**(객실·플랜·온천·코스·시설 이름/설명, 공지, 회원 문의·답변, 국가명, 푸터 주소 등): `GeminiTranslationService` + Thymeleaf 도우미 빈 `@tr`.
   - `${@tr.t(값)}` : 현재 언어로 번역, `${@tr.prefetch(목록, '속성'...)}` / `prefetchAll(...)` : 목록·상세 화면에서 여러 글을 API 1회로 미리 번역, `${@tr.label('접두어', 값)}` : `답변대기` 같은 상태값은 정해진 번역 키를 쓰고 없으면 번역.
   - **원문 언어는 자동 감지**(KO/EN/JA 어느 것이든). 이미 목표 언어인 글은 문자 종류로 판별해 API를 부르지 않음(한자만 있는 글은 일본어 화면에서만 그대로).
   - 캐시(메모리 + `translation-cache.json` 파일)로 재시작 후에도 재번역하지 않음. 파일은 `.gitignore` 처리. **번역이 마음에 안 들면 이 파일을 지우면 다시 번역됨.**
   - 모델이 번역하지 않고 원문을 그대로 돌려주는 경우가 실제로 있어(공지 제목 등) 그 경우 실패로 보고 다음 모델로 재시도.
   - API 키 없음/호출 실패 시 원문을 그대로 표시(화면은 깨지지 않음).
3. **서버 메시지**: 컨트롤러가 한글 문장 대신 메시지 키를 모델에 담고(`error.member.*`, `pay.error.*` 등) 템플릿이 `#{${error}}`로 번역. 결제 화면의 국가/도착시간 목록, "코스 포함·온천 이용" 문구도 키/`MessageSource` 기반으로 변경. JS `alert`/`confirm`은 `data-*` 속성 또는 `[[#{...}]]`로 번역.

**적용 화면:** 메인, 객실/온천/식사/시설, 교통안내, 공지 상세, 로그인/회원가입/마이페이지(예약현황·탈퇴 확인창), 1:1 문의(목록/작성/상세), 플랜 선택, 객실·식사·온천 선택, 결제/완료/실패, **관리자 화면 전체**(로그인/비밀번호 변경/계정 신청·승인, 정보등록, 예약 현황, 객실 현황, 판매 관리, 문의 관리, 교통안내 수정).

**관리자 화면:** 공용 셸(`admin_shell`)과 로그인/비밀번호 변경 화면에 KO/EN/JA 전환 버튼 추가(현재 주소의 다른 쿼리는 유지). 키는 `adm.*`. 고객이 쓴 문의·요청사항·신청 내용은 관리자의 언어로 번역해서 보여줌. **편집 폼의 입력값/`data-*`(수정 시 폼에 채워지는 원문)와 답변 입력창은 번역하지 않고 원문 그대로**라서 저장 시 번역문이 DB에 들어가지 않음. 관리자 계정 이름·예약자/신청자 이름 같은 데이터는 번역 대상이 아님.

**Gemini 모델 관련 (중요):**
- 기존 코드의 `gemini-2.0-flash`와 2.5 계열은 이미 신규 사용자에게 404(폐기). 목록 API로 확인 후 `gemini.models`(쉼표 구분, 앞에서부터 시도)를 `gemini-flash-lite-latest, gemini-3.1-flash-lite, gemini-flash-latest`로 설정. `gemini-flash-latest`만 쓰면 응답이 13초 걸리고 과부하(503)가 나서 lite를 앞에 둠. `GEMINI_MODELS` 환경변수로 덮어쓸 수 있음.
- **현재 키는 무료 티어라 모델당 분당 15회 제한(429)이 있음.** 콘텐츠가 처음 보일 때만 호출하고 이후엔 캐시라 평소엔 문제없지만, 새 콘텐츠를 한꺼번에 많이 등록하거나 새 서버(EC2)에서 캐시가 빈 채로 시작하면 처음 몇 분은 일부가 원문으로 보일 수 있음(재시도하면 채워짐). 유료 전환하면 해소.
- 키는 `GEMINI_API_KEY` 환경변수(로컬 사용자 환경변수로 설정됨). EC2에는 서비스 환경변수로 넣고, **캐시 파일을 쓸 수 있는 작업 디렉터리**에서 실행해야 함(`gemini.cache-file`로 경로 변경 가능).

**검증:** `mvnw clean compile` 성공, 단위 테스트(`GeminiTranslationServiceTest`, 언어 판별·키 없을 때 원문 반환) 통과. 메시지 키는 세 언어 모두 510개로 동일, 템플릿/Java가 참조하는 키 누락 없음. 관리자 화면은 로그인 없이 세션에 관리자 객체를 넣은 임시 MockMvc 테스트(커밋 안 함)로 실제 DB 데이터를 렌더링해 EN/JA/KO 8개 화면 모두 200, UI 문구 한글 0건 확인. 로컬 서버에서 실제 Gemini로 EN/JA/KO 각각 공개 화면 10개 + 로그인 상태의 마이페이지/문의/예약/결제 화면을 요청해 **화면에 남은 한글 0건** 확인. 일본어로 쓴 문의가 KO/EN/JA 모두에서 각 언어로 표시됨. 테스트 계정은 회원탈퇴로 삭제.

**알려진 한계:**
- **토스페이먼츠 결제 위젯(결제수단 선택/약관 UI)의 언어는 토스 쪽 화면이라 미적용.**
- 이메일 본문(관리자 승인 메일 등), 정책 페이지(`/policy/*` 링크만 있고 화면 없음)는 미적용.
- 관리자 화면의 정적 시연용 값(예약 현황의 "오늘 체크인 4" 같은 고정 숫자, 온천 일일 판매 한도 카드)은 원래 화면에 하드코딩된 시연 데이터라 문구만 번역함.
- 플랜 카드 이미지: DB의 `planImage`에 `["/uploads/..."]` 형태 JSON 문자열이 저장된 플랜이 있어 이미지 요청이 400/404가 남(번역과 무관한 기존 데이터 이슈).
- `.claude/launch.json`의 `sh` 실행 설정을 Windows에서 동작하도록 `cmd /c mvnw.cmd`로 변경.

---

## 팀원 3개 브랜치 재통합 (june47087-byte / eartth21 / yeseong) (2026-09-19)

**작업 방식:** `Choiyeongsu13`에서 갈라진 로컬 브랜치 `integrate-0919`에서 하나씩 병합. 원격에는 아직 push하지 않음.

**june47087-byte (+4커밋):**
- june가 `dto` 패키지를 `domain`으로 개명했으나 master/eartth21/기존 통합이 모두 `dto` 기준이라 병합 후 `domain → dto`로 되돌림 (85개 파일 import 정리). **june에게 dto 유지 공지 필요** (안 하면 다음 병합에서 또 충돌).
- `application.properties`에 june가 **Gmail 계정과 앱 비밀번호를 평문으로 커밋**해둠 → 환경변수(`MAIL_USERNAME`/`MAIL_PASSWORD`) 방식 유지, `admin.notify.email`만 반영. **이미 원격 브랜치 히스토리에 노출됐으므로 해당 앱 비밀번호는 폐기/재발급 권장.**
- Access/Notice/ReservationController는 june 쪽이 개명만 바꾼 것이라 우리 쪽(i18n·번역 포함) 채택.

**eartth21 (+4커밋):**
- 미사용 정리로 `PaymentMapper`(.java/.xml)를 삭제했으나 `PaymentReservationService`가 실제 사용 중이라 유지. (`CourseDto`, `DayStatusDto`, `PlanDto` 등 진짜 미사용 DTO 삭제는 그대로 반영)
- `index.html`: eartth21의 동적 료칸명 + 이미지 캐러셀에 우리 i18n 문구 결합. 관리자 페이지는 공용 `admin_shell` 구조 채택, `room_status.html`에서 빠진 `i-pencil`/`i-upload` 아이콘 정의 복구.
- 새 페이지 `/rooms`, `/onsen`, `/dining`, `/facility` 추가됨.

**yeseong (+3커밋):** 신규 커밋(자체 `ReservationService`, `domain/*DTO`, `mappers/` 패키지, 예약 화면)은 우리 쪽에 이미 공용 dto/mapper 구조로 통합돼 있고 로그인/결제 연동까지 되어 있음(화면·CSS·extraCharge·DB 호스트 모두 반영 확인). 그대로 병합하면 동명 매퍼 빈 중복으로 앱 기동 실패 위험이라 `-s ours`로 **병합 이력만 기록**하고 내용은 가져오지 않음. yeseong은 앞으로 공용 `dto`/`mapper` 클래스 위에서 작업하도록 안내 필요.

**검증:** `mvnw clean compile` 성공, 서버 기동 후 `/`, `/reservation/plan`, `/access`, 로그인/회원가입, 공지, `/rooms`·`/onsen`·`/dining`·`/facility`, 관리자 로그인 200 확인, 로그인 필요 페이지 302 리다이렉트 확인, 서버 로그 에러 0건. (관리자 로그인 후 화면과 실제 결제 흐름은 미검증)

---

## 회원가입/로그인/문의 기능 구현 + 4개 팀 브랜치 로컬 통합 (2026-09-18)

**배경:** 이 세션은 빌드가 깨진 상태(컴파일 에러)에서 시작. 패키지명 표기(대문자 vs 소문자)부터 팀원 4명(Choiyeongsu13/eartth21/june47087-byte/yeseong) 브랜치 통합, 당일 목표 기능(회원가입·로그인·문의작성, 비밀번호 해시) 구현까지 진행.

**초기 빌드 에러 수정:**
- `Inquiryservice.java` 파일명/클래스명 불일치, `com.mnu.Ryokanmaker.service` 패키지 오타, `mybatis.mapper-locations`가 존재하지 않는 `mappers/` 폴더를 가리키던 것을 `mapper/`로 수정.

**작업 중 대형 사고 및 복구:** 추적되지 않은 파일에 대한 `git mv`가 실패하면서 작업 트리 전체가 디스크에서 삭제되는 사고 발생. `git restore --source=HEAD -- .`으로 추적 파일은 복구하고, 미추적 파일(공지/문의 사용자 화면 등)은 대화 맥락과 다운로드 폴더에서 발견한 백업 zip으로 수동 재구성함. 이후로는 위험한 git 작업 전에 항상 `git add -A`로 먼저 스테이징해두는 방식으로 안전장치를 마련.

**패키지명 표기 정리:** 처음엔 `com.mnu.RyokanMaker`(대문자)로 통일하려 했으나, 팀원들이 이미 `com.mnu.ryokanmaker`(소문자)로 작업 중인 것을 확인하고 소문자로 재통일. Windows NTFS가 대소문자를 구분하지 않아 `git status`에 같은 경로가 다른 대소문자로 중복 추적되는 문제가 발생 → `git rm -r --cached .` 후 `git add -A`로 캐시를 초기화하고, 디렉터리명 변경은 전부 임시 이름을 거치는 2단계 rename(`mv X X_tmp && mv X_tmp Y`)으로 처리해 충돌을 피함.

**4개 팀 브랜치(마스터 제외) 로컬 통합:** 원격 저장소를 조사해 팀원별 구현 페이지/테이블을 파악하고, 사용자 지시대로 **원격에는 반영하지 않고 로컬 작업 트리에만** 각 브랜치의 내용을 순차적으로 반영. 통합 후 `mvnw clean compile` 성공 확인, Claude Browser로 홈/로그인/회원가입 등 주요 화면 직접 열람 테스트.

**DB 스키마 실측:** 기존 ERDCloud SQL 덤프가 실제 운영 DB와 다른 부분이 있어(예: `AdminDto`에 실존하지 않는 `ryokanFacility` 필드가 있던 것 등), SQL Developer Data Modeler로 실제 라이브 Oracle DB에서 DDL을 역공학(export)해 `sql/ryokan_schema_current.sql`로 저장하고 이를 기준 진실로 채택. 이 과정에서 DB 접속 호스트가 `54.180.103.132`(타임아웃)가 아니라 `3.36.211.118`로 바뀐 것도 팀원 커밋 메시지를 통해 발견해 반영.

**회원가입/로그인/문의 핵심 기능:**
- 비밀번호는 `PasswordUtil.sha256()`(레거시 `UserSHA256`과 동일한 해시 방식)로 해시 저장.
- 영문 이름은 알파벳만, 일본어 이름은 히라가나·가타카나만(한자 불가) 허용하도록 `NameValidationUtil`을 새로 만들어 서버 측 검증에 사용하고, `signup.html`/`mypage.html`의 이름 입력란에도 `pattern`/`title` 속성으로 동일한 제약을 추가(이중 방어).
- 회원가입 완료 후 **자동 로그인을 제거**하고 `/member/login?signup=success`로 리다이렉트, 로그인 화면에 가입완료 안내 배너 추가. 로그인은 반드시 회원이 직접 하도록 함.
- 마이페이지에 예약 현황(객실/온천/식당 예약 내역) 섹션 추가 — `ReservationMapper`(+xml)를 신규 작성해 `MemberService.getReservationHistory()`에서 사용.
- 1:1 문의 작성 폼의 내용 입력란을 라벨을 박스 위로 옮기고, 글씨를 더 크게, 박스도 직사각형으로 더 크게 조정(`common.css`의 `.form-row textarea`).
- 1:1 문의 관리자 화면(`/Admin/admin_inquiry`)에 관리자 로그인 여부 가드를 추가해 관리자만 열람 가능하도록 제한.
- 목록/상세 화면에서 제목·상태·등록일이 너무 붙어 보이던 문제를 `.content-actions`에 `gap` 추가 등으로 개선.
- 팀 공용 `common.css` 교체 과정에서 유실됐던 `.form-row`, `.field-label`, `.qna-table`, `.content-card` 등 클래스들을 다시 채워 넣어 폼/표 스타일 깨짐을 복구.

**회원탈퇴 + 문의 삭제 기능 신규 구현:** SQL Developer에서 테스트 계정을 지우려다 `ORA-02292`(FK 제약 위반, `FK_MEMBER_TO_ROOM_RESERVATION`/`FK_MEMBER_TO_INQUIRY`)가 발생한 것을 계기로, 자식 레코드부터 지우는 회원탈퇴 기능이 필요하다고 판단해 구현.
- `MemberController.withdraw()` (POST `/member/withdraw`) → `MemberService.withdraw()`가 `@Transactional`로 ROOM/ONSEN/RESTAURANT_RESERVATION → RESERVATION → INQUIRY → MEMBER 순서로 cascade 삭제 후 세션 무효화.
- `InquiryController`/`InquiryService`에 본인 소유 확인 후 개별 문의를 삭제하는 `delete()` 추가, `inquiry/view.html`에 확인창(`confirm()`)이 있는 삭제 버튼 추가.
- 마이페이지에 확인창이 있는 회원탈퇴 버튼 추가.

**Eclipse Lombok 미동작 문제 해결:** `mvn compile`은 성공하는데 Eclipse에서는 롬복 생성자가 인식 안 되는 문제 → 원인은 재설치 누락이 아니라 `SpringToolsForEclipse.ini`에 `-javaagent` 줄이 잘못 병합되어 있던 것(앞의 `-javaagent:...lombok.jar`와 뒤의 `-javaagent:...lombok.jar`가 한 줄에 붙어 있고 앞쪽엔 `-`가 빠져 있었음). 올바른 한 줄로 교체해 해결.

**git push 실패("↑8 ↓20") 해결:** 원인은 실제 커밋 충돌이 아니라, 앞서 "전체 삭제 후 새 파일 복사"로 통합하는 과정에서 로컬 브랜치의 커밋 그래프가 원격과 구조적으로 단절된 것이었음. `origin/Choiyeongsu13`을 기준으로 새 브랜치를 만들고, `git checkout <기존 커밋> -- <17개 파일>`로 이 세션에서 만든 고유 변경분만 다시 적용해 커밋 → `git rev-list --left-right --count`로 순수 fast-forward임을 확인 후 강제 push 없이 정상 push (`b05db9d..0feb1fe`) 완료.

**결과:** `mvnw clean compile` BUILD SUCCESS. Claude Browser로 회원가입(`finalcheck@example.com` 등 테스트 계정) → 가입완료 배너 → 로그인 → 마이페이지 예약현황/탈퇴 버튼 → 문의 작성/삭제까지 전체 플로우 직접 테스트 완료. 이후 다른 세션에서 push한 23개 커밋(다국어 지원, 교통안내 페이지, 결제/예약 전체 플로우, 방/플랜/온천/식당코스/시설 관리자 CRUD)을 fast-forward로 병합해 최신 상태 확인, 기존에 구현한 회원가입 기능이 병합 후에도 정상 동작함을 재확인.

---

## june47087-byte 브랜치 병합 + 교통안내(access) 페이지 개선 (2026-09-18)

**목표:** `origin/june47087-byte`(방/플랜/온천/시설/공지/문의답변 CRUD, 이미지 업로드, 실제 DB 연동 예약 화면이 대폭 구현된 브랜치)를 `Choiyeongsu13`으로 가져와서 로컬에서 통합 테스트.

**충돌 15개 파일 처리:**
- **세션 속성 키 재통일**: `Choiyeongsu13`은 `"loginAdmin"`, june 쪽은 `"admin"`을 쓰고 있었음. june 쪽이 방/플랜/온천/시설/공지/문의답변 등 훨씬 많은 화면이 `"admin"`에 의존하고 있어 `"admin"`으로 통일 (`AdminController`, `GlobalModelAdvice`, 관련 템플릿).
- **PW_RESET_YN 의미가 또 반전되어 있던 것 발견**: `Choiyeongsu13` 쪽 최신 코드는 `'Y'=초기 비밀번호(변경 필요)`로 되어 있었는데, june 쪽 최신 구현과 템플릿(`admin_pwreset.html`)은 `'Y'=비밀번호 변경 완료`로 정반대. june 쪽 의미로 통일 (`AdminService.authenticate/changePassword`, `AdminRequestService.approve`, `AdminMapper.xml`의 `updatePassword`).
- `WebConfig`(i18n LocaleResolver + 업로드 리소스 핸들러), `header.html`(i18n 유지), `InquiryMapper`/`InquiryService`(관리자 문의 답변 기능 추가) 등은 서로 다른 기능을 더한 것이라 양쪽 다 유지.
- 제가 만든 `/access`, `/Admin/access_edit`(교통안내 단독 수정 화면)은 june 쪽의 `admin_info_register` 내 "section-route"와 기능이 겹치지만 컬럼이 같아 충돌 없이 둘 다 남겨둠 (중복이지만 무해함).
- `mvnw compile` BUILD SUCCESS 확인. 로컬 구동 후 메인/plan/access/admin_login/admin_inquiry 리다이렉트까지 브라우저로 확인, 서버 로그에 에러 없음.

**교통안내(`access.html`) 개선:**
- 버스 노선 예시(오타루역앞 3번 승강장 → 순환버스 → '후루카와' 정류장, 약 10분·210엔), 택시 예상 요금(1,500~1,800엔), 도보 소요시간(약 15분)을 3칸 카드로 추가.
- 지도 SVG의 오타루역/清流庵 지점을 원형 마커 대신 지도 핀(물방울) 모양으로, 순환버스 정류장은 버스 아이콘으로 교체.
- 사용자 피드백으로 지도 그림 크기를 max-width 640px → 420px로 축소.
- 로컬 개발 편의를 위해 `spring.thymeleaf.cache=false` 추가 (서버 재시작 없이 html 수정 바로 반영).

**push 완료:** 병합 커밋 + 위 access 개선 커밋을 `origin/Choiyeongsu13`, `origin/Test` 양쪽에 fast-forward로 push 완료 (강제 push 없음).

**참고 — 히스토리 정리 관련:** june47087-byte 브랜치가 과거(제가 커밋 attribution을 정리하기 전)에 `Choiyeongsu13`을 한 번 병합해둔 적이 있어서, 이번 병합으로 attribution이 붙은 예전 커밋 6개가 `Test`/`Choiyeongsu13`에 다시 딸려 들어옴. 사용자 확인 결과 지금은 그대로 두기로 함 (필요시 나중에 정리 대상).

---

## dto→domain 패키지 통합 + 관리자 객실현황/판매관리 화면 완성 (2026-09-18)

**배경:** `AccessController.findByAdminIdx` 컴파일 에러 수정 후, `origin/yeseong` 브랜치를 로컬에 가져오려다 원격 브랜치 6개(Test/Choiyeongsu13/eartth21/master/yeseong) 상태를 점검. Test/Choiyeongsu13/eartth21은 이미 현재 브랜치(june47087-byte)의 조상이라 가져올 것이 없었고, master는 같은 커밋을 넣었다 revert해 실질 빈 커밋, yeseong만 "플랜선택 예약 구현" 등 고유 커밋 2개가 있었음.

**dto→domain 리네이밍:** yeseong이 `com.mnu.ryokanmaker.domain`(신규 DTO 5개)과 `com.mnu.ryokanmaker.dto`(기존 30개)가 혼재된 상태였고, 사용자 지시로 프로젝트 전체를 `domain`으로 통일하기로 함. `git mv`로 `dto` 디렉터리를 `domain`으로 옮기고 `sed`로 82개 파일(java+xml)의 패키지 참조를 일괄 치환, 빌드/테스트컴파일 성공 확인 후 커밋.

**yeseong 병합 보류:** Windows NTFS 대소문자 미구분 때문에 `CourseDTO.java`(yeseong) vs `CourseDto.java`(기존)가 같은 경로로 충돌해 `git merge`가 막힘. 내용 비교 결과 yeseong의 예약 로직(ReservationController/Service, DB 매퍼)은 현재 브랜치가 이미 더 발전된 버전(로그인 가드, Lombok DTO, 결제 연동)을 갖고 있어 병합하면 오히려 후퇴. 병합은 포기하고 yeseong에서 실질 가치 있는 CSS 2가지만 수동 반영: `.btn-selected` 스타일 추가, 식사 코스 카드를 2열 grid에서 가로 스크롤(캐러셀) 방식으로 변경.

**클린 빌드 필요성 발견:** dto→domain 리네이밍 직후 `mvnw spring-boot:run` 1회차에서 `NoClassDefFoundError: domain/CourseDTO (wrong name: CourseDto)` 발생 — 대소문자 미구분 파일시스템 때문에 `target/classes`에 stale 빌드 산출물이 남은 것. `mvnw clean compile`로 해결. Eclipse에서도 이런 리네이밍 후에는 Project → Clean이 필요함을 확인.

**관리자 객실현황(`/Admin/room_status`)/판매관리(`/Admin/plan_sales`) 완성:** 기존에는 하드코딩된 목업 화면이었음(반면 예약현황 `/Admin/reservation_status`는 이미 DB 연동 완료 상태였음).
- `RoomStatusService`/`PlanSalesService` 신규 작성: `RoomReservationMapper.findOverlapping()`을 재사용해 로그인한 관리자의 객실별/플랜별 7일 날짜별 예약 현황을 계산 (`RoomDayStatusDto`/`RoomStatusRowDto`/`PlanDayStatusDto`/`PlanSalesRowDto` 신규 DTO).
- `ROOM_SALE_YN`/`PLAN_SALE_YN` 토글 전용 매퍼 쿼리(`updateSaleYn`)와 서비스/컨트롤러 엔드포인트(`room_toggle_sale`/`plan_toggle_sale`) 추가.
- 두 화면 모두 `admin_info_register.html`의 등록/수정/삭제 CRUD 폼(이미지 업로드 포함)과 JS(이미지 미리보기, editRoom/editPlan)를 그대로 이식. 여러 화면에서 같은 저장/삭제 엔드포인트(`room_save`/`room_delete`/`plan_save`/`plan_delete`)를 쓰게 되어, `redirectTo` 파라미터(화이트리스트 검증)로 제출한 화면으로 되돌아가도록 처리.
- 이미지 저장은 기존 `ImageJsonUtil` 컨벤션(`src/main/resources/static/uploads/{room,plan}`) 그대로 사용 — 별도 변경 없음. `plan_sales`에서 올린 이미지는 `PLAN_IMAGE` 컬럼을 공유하므로 사용자 예약 화면(`planSelect.html`)에도 자동 반영됨.
- 사용자 결정: 플랜 판매중지 시에는 (버튼을 "선택불가"로 바꾸는 대신) 기존 동작대로 예약 화면에서 완전히 숨기는 것으로 유지. "온천 일일 판매 한도" 섹션은 이번 범위에서 제외하고 목업 그대로 복원.

**검증:** `mvnw clean compile` BUILD SUCCESS. 관리자 로그인(테스트 계정) 후 두 화면 모두 HTTP 200, 실제 DB 데이터(객실 5개, 플랜 3개)가 캘린더/카드에 정상 렌더링, 서버 로그 에러 없음 확인. 브라우저 스크린샷 도구 접근이 막혀 육안 확인은 못함.

---

## ORA-00904: 관리자 예약현황 상세(식사 코스) 조회 오류 수정 (2026-09-18)

**증상:** 관리자 예약현황(`/Admin/reservation_status`) 상세 조회 시 `ORA-00904: "C"."COURSE_IDX": 부적합한 식별자`.

**원인:** `AdminReservationMapper.xml`의 `selectRestaurantItems`가 `RESTAURANT_COURSE`를 `c`로 조인하면서 `c.COURSE_IDX`/`c.COURSE_NAME`을 참조했는데, 실제 컬럼명은 `RESTAURANT_COURSE_IDX`/`RESTAURANT_COURSE_NAME`임(사용자가 제공한 DB 익스포트 DDL로 확인). `RESTAURANT_RESERVATION` 쪽의 FK 컬럼명은 `COURSE_IDX`가 맞으므로 그쪽은 그대로 둠.

**수정:** `mappers/AdminReservationMapper.xml`의 조인 조건을 `c.RESTAURANT_COURSE_IDX = rr.COURSE_IDX`로, SELECT 절을 `c.RESTAURANT_COURSE_NAME AS courseName`으로 수정.

`mvnw clean compile` BUILD SUCCESS 확인. 이 파일의 다른 쿼리(ROOM/PLAN/ONSEN/MEMBER 조인)는 DDL과 대조해 이상 없음을 확인함.

---

## ORA-17004: 예약 저장(prepare) 실패 수정 (2026-09-18)

**증상:** payment.html에서 결제 버튼을 누르면 "예약 저장에 실패했습니다" 알림. Eclipse 콘솔에 `ORA-17004: 열 유형이 부적합합니다` 예외.

**원인:** `PaymentReservationService.saveAsWaiting()`은 `RESV_PAY_METHOD`를 세팅하지 않아 항상 `null`로 INSERT되는데(결제 전 '결제대기' 단계라 결제수단을 아직 모름), `PaymentMapper.xml`의 `#{resvPayMethod}`에 `jdbcType`을 지정 안 해서 MyBatis가 Oracle JDBC 드라이버에 `setNull(..., JdbcType.OTHER)`로 넘김 → Oracle 드라이버가 이 타입을 못 받아서 예외 발생. (`resvArrivalTime`/`resvRequest`도 같은 이유로 null이 될 수 있어 함께 방어.)

**수정:** `mappers/PaymentMapper.xml`의 `insertReservation`/`insertRoomReservation`에서 `#{resvPayMethod}` → `#{resvPayMethod,jdbcType=VARCHAR}`로 변경 (`resvArrivalTime`, `resvRequest`도 동일하게 `jdbcType=VARCHAR` 명시).

`mvnw clean compile` BUILD SUCCESS 확인. **아직 실제 예약 저장 재시도로 검증 안 함 — 다음에 결제 버튼 눌러서 RESERVATION/ROOM_RESERVATION에 행이 생기는지 확인 필요.**

---

## 예약 단계 로그인 체크 위치 확정 (2026-09-18)

**확정된 동작:**
- `/reservation/plan` (플랜 선택) — **비회원도 열람 가능.** 어떤 플랜이 있는지는 로그인 없이 볼 수 있어야 함.
- `/reservation/reservation` (객실·식사·온천 선택) — **로그인 필수.** 없으면 `/member/login`으로 리다이렉트.
- `/payment` — 로그인 필수 (앞 항목에서 이미 적용됨).

즉 플랜 카드를 눌러 다음 단계로 넘어가는 시점에 비회원이 로그인 페이지로 이동한다.

**수정:** `ReservationController.reservation()`에 `session.getAttribute("loginMember")` 체크 추가. `planSelect()`는 체크 없음.

**참고:** 처음엔 `/reservation/plan`에도 체크를 넣었다가, 플랜 목록은 비회원에게도 보여야 한다는 요구로 되돌림.

`mvnw clean compile` BUILD SUCCESS 확인.

---

## 예약 내용이 DB에 저장되지 않던 문제 해결 (2026-09-18)

**증상:** 결제까지 진행해도 RESERVATION / ROOM_RESERVATION 테이블에 아무 행도 안 생김.

**원인:** `PaymentMapper`(+xml)에 `insertReservation` / `insertRoomReservation`이 이미 다 구현돼 있었는데 **컨트롤러 어디에서도 호출하지 않았음.** `PaymentController.payment()`에 `TODO: 결제 담당자 - ... RESERVATION/ROOM_RESERVATION 등 INSERT 로직 추가` 주석만 남아 있는 상태였고, `/payment/prepare`도 "지금은 화면 확인용이라 세션에만 보관" 상태였음.

**DB 실측(사용자 제공 익스포트 DDL)으로 확정한 제약 — 기존 WORKLOG의 ERD 기록과 다름:**
- `RESERVATION.USER_MAIL` → `MEMBER.USER_MAIL` **FK 존재** (NOT NULL)
- `ROOM_RESERVATION.USER_MAIL` → `MEMBER.USER_MAIL` **FK 존재** (NOT NULL)
- `ROOM_RESERVATION.RESV_NUM` → `RESERVATION.RESV_NUM` FK, `ROOM_IDX`/`PLAN_IDX`/`ADMIN_IDX`도 전부 FK
- 즉 **비회원 예약은 DB 구조상 불가능** (MEMBER에 없는 이메일은 FK 위반). 사용자와 협의해 **로그인 필수**로 확정.
- 저장 순서는 반드시 RESERVATION → ROOM_RESERVATION (FK 때문).

**구현한 흐름 (사용자가 선택한 방식: prepare에서 먼저 저장 → success에서 갱신):**
1. `/payment` 진입 시: 로그인(`session.loginMember`) 확인, 없으면 `/member/login`으로 리다이렉트. 선택값(플랜/객실/날짜/인원/코스/온천)을 `ReservationContext`로 묶어 `RESERVATION_CONTEXT_<orderId>` 키로 세션에 저장.
2. `/payment/prepare`(결제창 띄우기 직전): 세션의 컨텍스트 + 예약자 폼으로 **RESERVATION과 ROOM_RESERVATION을 `결제대기` 상태로 INSERT**.
3. `/payment/success`(토스 콜백): 서버 신뢰 금액을 **세션이 아니라 DB(`selectResvPriceByOrderId`)에서 조회**해 위변조 검증 → `confirm()` 호출 → 두 테이블을 `결제완료` + 결제수단으로 UPDATE.

**새로 만든 파일:**
- `dto/ReservationContext.java` — /payment에서 확정된 선택값을 세션에 담는 객체.
- `service/PaymentReservationService.java` — `@Transactional`로 RESERVATION→ROOM_RESERVATION 저장(`saveAsWaiting`), 결제완료 갱신(`markAsPaid`), 신뢰금액 조회(`findTrustedAmount`). 상태 문자열 상수(`예약완료`/`결제대기`/`결제완료`)도 여기 모음.

**수정한 파일:**
- `controller/PaymentController.java` — 위 1~3 흐름 구현. `prepare`를 `ResponseEntity`로 바꿔 401(로그인 만료)/400(세션 만료)/500(저장 실패)을 구분해 응답. 기존 `SESSION_ORDER_AMOUNT_PREFIX` 세션 금액 검증은 DB 조회로 대체해 제거.
- `mapper/PaymentMapper.java` + `mappers/PaymentMapper.xml` — `updateRoomPayStatusByResvNum`, `selectResvNumByOrderId` 추가 (기존 `updatePayStatusByOrderId`는 RESERVATION만 갱신해서 ROOM_RESERVATION이 `결제대기`로 남는 문제가 있었음).
- `templates/reservation/reservation.html` — "결제 페이지로 이동" JS가 `adultCount`/`childCount`/`roomCount`를 쿼리에 실어 보내도록 추가 (기존엔 안 넘겨서 결제 화면 인원이 항상 하드코딩 2명/0명이었음 — 미해결 항목 "결제페이지 객실수·인원" 도 이걸로 함께 해소).
- `templates/payment/payment.html` — prepare 응답이 401이면 로그인 페이지로 보내도록 분기.
- `controller/PaymentController.payment()` — `ReservationSummary`의 roomCount/adultCount/childCount 하드코딩(1,2,0)을 실제 파라미터 값으로 교체.

**`mvnw clean compile` BUILD SUCCESS 확인 완료.**

**아직 실제 동작 확인 안 됨 — 다음에 할 일:**
- 실제 로그인 → 플랜선택 → 예약 → 결제(토스 테스트키) 골든패스로 RESERVATION/ROOM_RESERVATION에 행이 생기는지 확인 필요.
- 결제창을 띄웠다가 취소하면 `결제대기` 행이 남는다(선택한 방식의 트레이드오프). 나중에 미결제 예약 정리 배치나 관리자 화면에서의 처리 방법 논의 필요.
- 온천/식사 선택값(`courseIdx`/`onsenIdx`/`onsenTimeSlot`)은 `ReservationContext`에 담아두긴 했지만 `ONSEN_RESERVATION`/`RESTAURANT_RESERVATION` 테이블에는 아직 저장 안 함. (두 테이블 모두 `RESTAURANT_SIDEMENU` NOT NULL 등 추가 입력값이 필요해서 화면 설계 확인 후 별도 작업 필요.)
- DB의 `MEMBER` 테스트 데이터 중 `tanaka.yuki@example.jp`는 비밀번호가 평문(`testpass`)이라 로그인 불가. `june47087@gmail.com`은 sha256이라 정상. 테스트 시 후자 사용할 것.

---

## PW_RESET_YN 방향 재정정 (2026-09-18, 바로 아래 "로그인 실패 버그 수정" 항목의 후속 수정)

**바로 아래 섹션에서 정한 N=평문/Y=해시 분기 조건 자체는 맞았지만, N↔Y가 언제 세팅되는지를 반대로 적용했었음.** 사용자가 명확히 확정한 실제 규칙:

- **관리자 최초 가입(계정 생성) 직후 `PW_RESET_YN` 디폴트 = `'N'`** → 비밀번호 **평문** 저장, 로그인 시 평문 비교.
- **비밀번호를 한 번이라도 변경하면 `PW_RESET_YN` → `'Y'`로 바뀜** → 그 이후부터 비밀번호는 **sha256 해시**로 저장, 로그인 시 해시 비교.

**추가 수정한 파일 (아래 섹션에서 처음 고친 것에 이어서):**
- `AdminController.adminLogin()`: 로그인 성공 후 비번변경 화면으로 강제 이동시키는 조건을 `"Y".equals(pwResetYn)` → `!"Y".equals(pwResetYn)`(즉 `N`일 때, 최초가입=평문 상태일 때 강제 이동)으로 반전.
- `AdminController.passwordReset()`: 비밀번호 변경 성공 후 세션에 세팅하던 `pwResetYn`을 `"N"` → `"Y"`로 변경.
- `AdminService.changePassword()`: 현재 비밀번호 검증을 평문 비교로, 새 비밀번호 저장을 sha256 해시로 반전(기존에 짰던 것과 정반대).
- `AdminMapper.xml`의 `updatePassword`: `PW_RESET_YN = 'N'` → `'Y'`로 갱신하도록 SQL 수정. `AdminMapper.java` 주석도 동일하게 정정.
- `AdminRequestService.approve()`: 임시 비밀번호 발급 시 **평문**으로 저장하고 `pwResetYn`을 `"N"`으로 세팅하도록 반전(기존엔 sha256+`"Y"`였음 — 이것도 반대 방향으로 짜여 있었음).
- `admin_pwreset.html`: 초기 비밀번호 안내 배너 조건 `pwResetYn=='Y'` → `pwResetYn!='Y'`(즉 아직 `N`인 최초 상태일 때 안내 노출).
- `mvnw clean compile` 재확인 완료.

---

## 관리자 로그인 실패 버그 수정 (2026-09-18)

**⚠️ 이 섹션의 N/Y 방향은 틀렸었고, 바로 위 "PW_RESET_YN 방향 재정정" 섹션에서 반대로 고쳐졌음. 최종 규칙은 위 섹션 참고.**

**증상:** DB에 정상 가입된 아이디/비밀번호로 `/Admin/admin_login`을 호출해도 항상 "아이디 또는 비밀번호가 일치하지 않습니다" 에러로 빠짐.

**원인:** 바로 위 섹션("eartth21 + Choiyeongsu13 병합 완료")에서 `PW_RESET_YN` 의미를 확정할 때, 정작 `AdminService.authenticate()`/`changePassword()`는 `PW_RESET_YN` 값과 무관하게 **항상 sha256 해시로만 비교**하도록 남아있었음. 그런데 사용자가 실제로 운영하는 방식은:
- `PW_RESET_YN = 'N'` (정상 비밀번호로 변경 완료) → DB에 **평문**으로 저장
- `PW_RESET_YN = 'Y'` (관리자 승인 시 발급된 임시 비밀번호, 최초 로그인 후 변경 강제) → DB에 **sha256 해시**로 저장 (`AdminRequestService.approve()`가 이렇게 저장함)

즉 평문으로 저장된(`N`) 계정에 sha256 비교를 걸어서 항상 실패했던 것.

**수정 내용:**
- `AdminService.authenticate()`: `admin.getPwResetYn()`이 `"Y"`면 입력값을 sha256 해시해서 비교, 아니면(`"N"`) 평문 그대로 비교하도록 분기 추가.
- `AdminService.changePassword()`: 이 메서드는 항상 `PW_RESET_YN='Y'`(해시 저장) 상태에서만 호출되므로 현재 비밀번호 검증은 기존대로 sha256 유지. 다만 변경 후에는 `updatePassword()`가 `PW_RESET_YN`을 `'N'`으로 바꾸므로(평문 저장 규칙), 새 비밀번호를 **평문으로 저장**하도록 변경 (`PasswordUtil.sha256(newPassword)` 제거).
- `mvnw clean compile` 재확인 완료 (에러 없음).

**참고:** `AdminRequestService.approve()`(임시 비밀번호 발급 시 sha256 저장 + `pwResetYn="Y"`)는 그대로 두어도 이 규칙과 일치하므로 손대지 않음.

---

## eartth21 + Choiyeongsu13(신규분) 병합 완료 (2026-09-18)

**결과:** eartth21 병합 커밋(`a1c8ee5`), Choiyeongsu13 신규분(`6b34050` 등) 병합 커밋(`c5c877f`) 완료. 둘 다 `mvnw clean compile` BUILD SUCCESS 확인.

**eartth21 병합에서 처리한 것:** 아래 "eartth21 병합 진행 중" 섹션(당시 작업 로그) 참고. domain 패키지 삭제 후 dto로 통합, PlanDto→AdminPlanDto 리네임, Inquiry/NoticeService 사용자+관리자 메서드 통합, AdminController에 세션로그인+CRUD+예약현황 통합, admin_login/admin_pwreset/admin_info_register.html의 스네이크케이스·#session 버그 수정.

**Choiyeongsu13 신규분 병합에서 추가로 발견/처리한 것:**
- **PW_RESET_YN 의미가 eartth21과 정반대였음.** Choiyeongsu13의 AdminRequestService(관리자 계정승인 시 임시비번 발급)가 `pwResetYn="Y"`를 "초기 비밀번호라 변경 필요"로 명확히 문서화하고 있어 이것이 정답으로 확정. eartth21이 만들었던 AdminController/AdminService의 반대 방향 로직(`"N"`을 초기상태로 착각)을 전부 뒤집어 수정함.
- **세션 속성 키 충돌**: eartth21은 `"admin"`(26곳에서 사용), Choiyeongsu13은 `"loginAdmin"`(3곳). 수정 범위가 작은 `"admin"`으로 통일 — Choiyeongsu13 쪽 로그인/로그아웃 로직만 맞춰 고침.
- **예약현황 라우팅 버그 발견 및 수정**: 모든 admin 템플릿의 사이드바 링크는 실제로 `/Admin/reservation_status`를 가리키는데, eartth21 병합 때 실수로 `/Admin/admin_reservation`으로 되돌려놨던 것을 바로잡음.
- AdminService 메서드명을 Choiyeongsu13 기준(`authenticate`, `changePassword`, `PasswordUtil` 사용)으로 통일하고 이제 안 쓰는 `UserSHA256.java` 삭제.
- InquiryService/Mapper에 회원 문의 삭제(`deleteByIdx`/`deleteByUserMail`, 회원탈퇴 연동) 추가.
- `admin_info_register_b.html`(구버전, 컨트롤러 어디서도 참조 안 함)과 `admin_requests.html`/`admin_apply.html`(이전 Choiyeongsu13 병합 때 들어온 관리자 신청 승인 화면)은 이번 작업 범위 밖이라 손대지 않음 — 필요시 별도 정리 대상.

**다음 할 일:**
- 원격(`origin/june47087-byte`)으로 push 여부 확인 필요 — 아직 push 안 함.
- Eclipse에서 실제 로그인 → 비밀번호 변경 → 관리자 정보 등록 화면까지 골든 패스 동작 확인 권장 (pwResetYn 의미를 뒤집었으므로 실제 DB 데이터 기준으로도 재확인 필요).

---

## yeseong 병합 — 진행 중, 커밋 전 상태 (2026-09-18)

**⚠️ 중요: 아직 `git merge origin/yeseong`을 실행하지 않았음.** 지금까지 한 작업은 yeseong 브랜치의 변경 내용을 파악해서 **워킹트리에 수동으로 이식**한 것이고, 실제 merge 커밋은 생성 안 됨. `git status`에는 M/D/??(수정/삭제/신규)만 있고 아직 커밋되지 않은 상태. 다른 세션에서 이어받을 경우 아래 "이어서 할 일"부터 시작할 것.

**상황 파악 (eartth21/Choiyeongsu13과 동일 패턴):**
- yeseong도 merge-base(`d2523f3`)가 오래돼서 HEAD의 dto/mapper 구조 발전을 못 따라감. 고유 커밋은 `617df68`(0916 은예성), `c60d7dc`(플랜선택 예약 구현) 2개.
- yeseong은 `domain` 패키지에 `PlanDTO`/`RoomDTO`/`OnsenDTO`/`CourseDTO`/`RoomReservationDTO`(전부 Long 타입)를, `mappers`(복수형!) 패키지에 대응 매퍼를 새로 만들었음 — 필드 구성이 우리 `dto`/`mapper` 패키지(Integer 타입)의 `AdminPlanDto`/`RoomDto`/`OnsenDto`/`RestaurantCourseDto`/`RoomReservationDto`와 사실상 동일한 테이블을 가리킴. eartth21 때처럼 domain/mappers 쪽은 버리고 기존 dto/mapper 인프라에 조회 메서드만 추가하는 방식으로 이식.
- **사용자가 제공한 `payment()` 컨트롤러 스니펫**(planCode/roomIdx/checkIn/checkOut/totalAmount/courseIdx/onsenIdx/onsenTimeSlot 파라미터)은 yeseong이 새로 만든 `reservation/reservation.html`의 "결제 페이지로 이동" 버튼 JS가 정확히 이 파라미터들을 만들어 `/payment?...`로 보내는 것과 세트임을 확인 — 기존 `PaymentController.payment()`(더미데이터 GET)를 이 스니펫으로 교체하고 예약화면과 연결하는 게 목표였음.

**이미 완료한 이식 작업 (워킹트리에 반영됨, 아직 미커밋):**
1. `dto/RoomAvailabilityDto.java`, `dto/BathAvailabilityDto.java`: yeseong 구조(필드 추가: roomIdx, roomPeople, roomPrice, extraCharge, available / onsenIdx)로 교체, Lombok `@Data`로 재작성. 기존 사용처(`ReservationController`+구버전 `booking.html`)뿐이라 안전하게 교체 가능함을 확인.
2. `dto/AdminPlanDto.java`: `planSaleYn` 필드 추가 (PLAN_SALE_YN 컬럼 대응, 기존엔 빠져있었음).
3. `mapper/RoomMapper.java`+`.xml`, `mapper/PlanMapper.java`+`.xml`, `mapper/OnsenMapper.java`+`.xml`, `mapper/RestaurantCourseMapper.java`+`.xml`: 각각 `findAllOnSale()`(SALE_YN='Y' 필터, 사용자 화면 노출용) 추가. Room/Plan은 `findById()`, Room은 `findMinPriceByLevel()`도 추가.
4. `mapper/RoomReservationMapper.java`+`.xml` 신규 생성 (HEAD에 없었음) — `findOverlapping(rangeStart, rangeEnd)`.
5. `service/ReservationService.java` 신규 생성 — yeseong 로직을 우리 dto/mapper로 재작성 (`getAllPlans`, `getCourses`, `getRoomAvailability`, `getBathAvailability`, `calculateFinalPrice`).
6. `templates/reservation/planSelect.html`: yeseong의 DB연동 버전(AdminPlanDto 필드 기준)으로 교체, 링크를 `/reservation/reservation`으로, 버튼 스타일은 HEAD 것 유지.
7. `templates/reservation/booking.html` 삭제 (→ `reservation.html`로 대체).
8. `templates/reservation/reservation.html` 신규 추가 (yeseong 버전 그대로 + `restaurantCourseSaleYN`→`restaurantSaleYn` 필드명만 우리 DTO에 맞게 수정).
9. `controller/ReservationController.java`: HEAD의 `mainIndex()`(공지사항 미리보기) 유지 + yeseong의 `/reservation/plan`, `/reservation/reservation`(구 `/reservation/booking` 대체)을 `ReservationService` 기반으로 재작성.
10. `controller/PaymentController.java`: `payment()` GET을 사용자가 준 스니펫 시그니처로 교체. `PlanMapper`/`RoomMapper`/`RestaurantCourseMapper`를 주입해 실제 플랜/객실/코스 이름을 조회하고, `ReservationSummary`를 실제 파라미터 기반으로 채우도록 구현. `totalAmount`를 세션에 저장해서(`TOSS_ORDER_AMOUNT_` + orderId) `/payment/prepare`가 더 이상 하드코딩 848508을 쓰지 않고 이 값을 쓰도록 연결(`prepare`의 세션 세팅 라인 제거, 주석 정리). TODO 주석("결제 담당자 - 회원 정보 조회, 결제 수단 처리, RESERVATION/ROOM_RESERVATION INSERT")은 원본 스니펫 그대로 유지.
11. `mvnw clean compile` → **BUILD SUCCESS** 확인 완료.

**이어서 할 일 (다른 세션에서 시작할 경우 여기부터):**
1. **먼저 `git status`로 현재 워킹트리 상태 재확인** (위 "이미 완료한 이식 작업" 파일들이 그대로 있는지).
2. 지금 상태는 실제 `git merge origin/yeseong`을 실행한 게 아니라 수동 이식이므로, 두 가지 방법 중 선택 필요:
   - (A) 이 상태 그대로 일반 커밋으로 남기고, `git merge -s ours origin/yeseong`으로 yeseong을 "병합된 것"으로 히스토리에 기록만 하기 (충돌 재발 방지, 다만 실제 병합 diff는 안 남음).
   - (B) 지금 변경사항을 임시로 커밋/스택시 해두고 `git merge origin/yeseong`을 실제 실행해서 진짜 충돌을 보고, 이미 준비한 내용으로 해소하기 (더 정직한 히스토리, 시간 더 걸림).
   - **사용자에게 어느 방식 원하는지 먼저 물어볼 것.**
3. `admin_info_register.html`의 room/onsen/plan/facility 목록에서 이번에 추가한 `findAllOnSale()`류가 실수로 관리자 화면에도 영향 주지 않는지 확인 (관리자 화면은 `selectXxxByAdmin`을 그대로 쓰고 있어 영향 없어야 함 — 재확인만 하면 됨).
4. `payment.html`이 `reservation.getRoomCount()`(항상 1로 하드코딩됨), `adultCount`/`childCount`(2/0 하드코딩됨) 등 여전히 일부 값은 고정값 — 이 부분은 이번 작업 범위 밖(원래 TODO)이므로 그대로 두되, 필요시 후속 작업으로 안내.
5. 최종 병합 커밋 메시지 작성 (eartth21/Choiyeongsu13 커밋과 같은 형식으로 상세히).
6. `mvnw clean compile` 재확인 후 커밋.
7. 세 브랜치(eartth21/Choiyeongsu13/yeseong) 모두 병합 완료 후, 원격(`origin/june47087-byte`)으로 push할지 사용자에게 확인.

---

## eartth21 병합 진행 중 (2026-09-18, 작업 로그)

**목표:** origin(tenyah/RyokanMaker.git)의 `eartth21`, `yeseong`, `Choiyeongsu13` 브랜치를 `june47087-byte`로 통합. 이 중 `eartth21`부터 처리 중.

**상황 파악:**
- `git ls-remote --heads origin`으로 확인한 결과 세 브랜치 모두 팀원 개인 fork가 아니라 origin(tenyah/RyokanMaker) 안의 브랜치였음 (개인 fork로 착각해 `eartth21`/`Choiyeongsu13` fork를 remote로 추가했다가 삭제, `yeseong`이라는 이름의 fork는 애초에 존재하지 않았음).
- `eartth21` 브랜치는 merge-base(`a342554`)가 매우 오래돼서 HEAD의 대규모 리팩터링(DTO를 `domain/*.java`(대문자 DTO, plain)에서 `dto/*.java`(소문자 Dto, Lombok `@Data`)로 전환한 것)을 전혀 반영하지 못한 상태. eartth21 고유 커밋은 `dee4c3f`(어드민), `7e56674`(어드민 수) 2개.
- `git merge origin/eartth21` 시도 → 13개 파일 충돌(UU/DU/UD/AA). 분석 결과:
  - `domain/AdminDTO.java` 등 domain 패키지 DTO 7종은 HEAD의 `dto/*.java`와 필드가 대부분 동일 → domain 쪽은 버리고 dto 쪽에 흡수.
  - `OnsenDto`/`RestaurantCourseDto`/`RoomDto`는 eartth21 쪽이 필드(`onsenHour`, SaleYn 등)와 헬퍼 메서드(`getThumbnailUrl()` 등)가 더 완성도 높음 → eartth21 기준으로 `dto` 패키지에 재작성, 필드명은 소문자 `Yn` 통일(`onsenSaleYn`/`restaurantSaleYn`/`roomSaleYn`).
  - `FacilityDto`는 HEAD에 아예 없던 것 → eartth21 버전 그대로 `dto` 패키지에 추가 예정.
  - **`PlanDto` 이름 충돌 발견**: HEAD의 `dto.PlanDto`는 사용자 예약 화면(플랜 선택, `ReservationController`)용이고 eartth21의 `domain.PlanDto`는 관리자 플랜 마스터 등록용 — 완전히 다른 목적의 동명 클래스. eartth21 쪽을 `AdminPlanDto`로 리네임하기로 결정.
  - `InquiryService`/`NoticeService`(및 Mapper/xml)는 AA 충돌 — HEAD는 회원(사용자)측 조회/작성 메서드, eartth21은 관리자측 목록/답변/CRUD 메서드로 서로 보완 관계. 두 메서드 세트를 합칠 예정.
  - `AdminController`/`AdminService`/`AdminMapper`는 eartth21의 세션 로그인/비번재설정 흐름과 HEAD의 기존 예약현황(`admin_reservation`) 로직을 합쳐서 하나로 정리 예정.
  - 템플릿(`admin_*.html`)은 파일별로 비교 후 병합 예정.

**진행 완료 (2026-09-18):**
- `dto/OnsenDto.java`, `dto/RestaurantCourseDto.java`, `dto/RoomDto.java`를 eartth21 기준(헬퍼 메서드 포함, SaleYn 소문자 통일)으로 재작성 완료.

**다음 할 일 (2026-09-18 이후 이어서 진행):**
1. `dto/FacilityDto.java` 신규 추가, `dto/AdminPlanDto.java` 신규 추가(eartth21 domain.PlanDto 리네임).
2. eartth21이 새로 만든 서비스/매퍼(`RoomService`, `OnsenService`, `PlanService`→AdminPlanDto로, `FacilityService`, `RestaurantCourseService`, 각 Mapper.java/xml)의 `domain.*Dto` import를 `dto.*Dto`로 전환.
3. `InquiryService`/`NoticeService`/`InquiryMapper`/`NoticeMapper`(.java/.xml) AA 충돌 해소 — 사용자측+관리자측 메서드 합치기.
4. `AdminController`/`AdminService`/`AdminMapper` UU/DU 충돌 해소 — 세션 로그인 + 예약현황 로직 통합, `PlanDto`→`AdminPlanDto` 참조로 교체.
5. `application.properties` UU 충돌 해소 (과거처럼 datasource 포트 등 로컬값 유지 여부 확인).
6. 템플릿(`admin_info_register.html` 등) 병합 상태 재확인.
7. `domain/*.java` 잔존 파일 삭제, `.DS_Store` 등 불필요 파일 제외.
8. `mvnw clean compile`로 빌드 확인 후 병합 커밋.
9. eartth21 완료 후 `yeseong` 브랜치 병합 — 이때 사용자가 제공한 `payment()` 컨트롤러 스니펫(`planCode`/`roomIdx`/`checkIn`/`checkOut`/`totalAmount`/`courseIdx`/`onsenIdx`/`onsenTimeSlot` 파라미터 받는 결제 화면 진입 메서드, TODO로 결제 로직 표시)을 참고해서 처리할 것.
10. 이후 `Choiyeongsu13`은 이미 이전에 병합 완료(`0216ccb`)돼 있으나 origin에 `6b34050`(09/17) 신규 커밋이 추가로 있어 재확인 필요.

---

## Choiyeongsu13 병합 완료 (2026-09-16)

**목표:** origin(tenyah/RyokanMaker.git)의 `Choiyeongsu13` 브랜치(팀원 통합본, 대소문자/스펠링 주의 — "choiyeonsu13" 아님)를 현재 브랜치(`june47087-byte`)로 가져오기.

**처리 결과:**
1. `plan_sales.html`의 탭 문자 diff는 의도치 않은 것으로 판단, `git checkout --`으로 되돌림.
2. `WORKLOG.md` + `RestaurantCourseDto.java` 변경사항 커밋 (`4455997`).
3. `git fetch origin Choiyeongsu13` → `git merge FETCH_HEAD` — **충돌 없이 자동 병합 성공.**
4. `application.properties`의 `spring.datasource.url` 포트가 예상대로 1521로 들어와 있어 로컬 유지값 **2200으로 되돌림.**
5. 병합 커밋 완료 (`0216ccb`).
6. `mvnw clean compile` → **BUILD SUCCESS** (52개 소스 파일, TossPaymentService의 기존 unchecked 경고만 있음, 무관).

**병합으로 들어온 주요 변경:** MemberController/Service(회원가입), AdminRequestController/Service+EmailService(관리자 신청·승인), COUNTRY_CODE 테이블/매퍼, 국가/전화코드 UI, `AdminDTO.java`(domain) 삭제 후 `AdminDto`(dto)로 정리, mail 설정 등. B담당(예약/객실/판매) 파일은 영향받지 않음.

**다음 할 일:** 원격(`origin/june47087-byte`)으로 push할지 확인 필요 — 아직 push 안 함(로컬에 1 commit ahead 상태였음, 병합 커밋까지 포함하면 2 commit ahead). Eclipse에서 새로 들어온 회원가입/관리자신청 기능 정상 동작 확인 필요.

---

## (이전 기록 — 병합 전 조사 내용, 참고용)

**확인된 사실:**
- `Choiyeongsu13`은 현재 HEAD(`a9e8f10` "관리자 페이지 통합")에서 13개 커밋 앞서 있음. 회원가입(MemberController/Service), 관리자 신청·승인(AdminRequestController/Service, EmailService), COUNTRY_CODE 테이블, 국가/전화코드 UI 등이 추가됨.
- B담당(예약/객실/판매, 우리가 작업한 파일) 쪽은 건드리지 않은 것으로 보임 — 충돌 위험 낮음.
- **주의:** `application.properties`가 Oracle 포트를 `1521`로 바꿔놓음. 우리 로컬은 `2200` 유지 중이었음(과거에도 이 충돌 있었음, 병합 시 로컬값 2200 유지하기로 했었음) — 병합 시 이 줄만 되돌릴 것. 메일 발송 설정(`spring.mail.*`, MAIL_USERNAME/PASSWORD 환경변수 기반)이 새로 추가되는데 이건 그대로 받아도 됨.
- 커밋되지 않은 현재 변경사항 3개 있음: `WORKLOG.md`, `RestaurantCourseDto.java`(필드명 DB 컬럼 기준 정정, 완료된 작업), `plan_sales.html`(탭 문자 1개 추가된 사소한 diff, 원인 불명 — 사용자 확인 대기 중이었음).

**다음에 실행할 명령 (순서대로):**
```sh
# 1. 지금 변경사항 먼저 커밋 (plan_sales.html 탭 diff는 사용자가 원치 않으면 checkout으로 되돌리고 커밋)
git status
git add WORKLOG.md src/main/java/com/mnu/ryokanmaker/dto/RestaurantCourseDto.java
git commit -m "실제 DB 컬럼 실측 기록 + RestaurantCourseDto 필드명 정정"

# 2. Choiyeongsu13 브랜치 fetch (원격엔 원격추적 브랜치가 아직 없어서 FETCH_HEAD로 받힘)
git fetch origin Choiyeongsu13

# 3. 병합 (--no-edit 쓰지 말 것 — merge 커밋 메시지 직접 확인)
git merge FETCH_HEAD

# 4. 충돌 나면 application.properties의 datasource.url만 2200으로 되돌리기
#    (다른 충돌 없을 것으로 예상되지만 발생 시 파일별로 확인)

# 5. 빌드 확인
mvn clean compile
```

- (여기에 작업 내용 기록)
- ERD(CREATE TABLE 원본, 카멜케이스 표기) 기록 및 실제 DB 컬럼 확인용 쿼리, IDENTITY 자동생성 시퀀스↔테이블 매핑 확인용 쿼리 추가.

### ERD (원본 CREATE TABLE 스크립트, 표기는 실제 DB 컬럼명과 다를 수 있음 — 실제 컬럼명은 아래 쿼리로 확인)

```sql
CREATE TABLE `ROOM_RESERVATION` (
	`Room_Resv_num`	NUMBER	NOT NULL,
	`User_Mail`	VARCHAR2(100)	NOT NULL,
	`Admin_Idx`	NUMBER	NOT NULL,
	`Room_Idx`	NUMBER	NOT NULL,
	`Resv_num`	NUMBER	NOT NULL,
	`Plan_Idx`	NUMBER	NOT NULL,
	`Resv_Check_In`	DATE	NOT NULL,
	`Resv_Check_Out`	DATE	NOT NULL,
	`Resv_Price`	NUMBER	NOT NULL,
	`Resv_People`	NUMBER	NOT NULL,
	`Resv_Status`	VARCHAR2(20)	NOT NULL,
	`Resv_Pay_Status`	VARCHAR2(20)	NULL,
	`Resv_Pay_Method`	VARCHAR2(20)	NULL
);

CREATE TABLE `MEMBER` (
	`User_Mail`	VARCHAR2(100)	NOT NULL,
	`User_Password`	VARCHAR2(200)	NOT NULL,
	`User_Nickname`	VARCHAR2(50)	NOT NULL,
	`User_Country`	VARCHAR2(50)	NULL,
	`User_Address`	VARCHAR2(300)	NULL,
	`User_Tel`	VARCHAR2(30)	NULL,
	`User_Last_Name_En`	VARCHAR2(50)	NOT NULL,
	`User_First_Name_En`	VARCHAR2(50)	NOT NULL,
	`User_Last_Name_Jp`	VARCHAR2(50)	NULL,
	`User_First_Name_Jp`	VARCHAR2(50)	NULL
);

CREATE TABLE `ROOM` (
	`Room_Idx`	NUMBER	NOT NULL,
	`Admin_Idx`	NUMBER	NOT NULL,
	`Room_Name`	VARCHAR2(100)	NOT NULL,
	`Room_Level`	VARCHAR2(50)	NOT NULL,
	`Room_Info`	VARCHAR2(1000)	NULL,
	`Room_Price`	NUMBER	NOT NULL,
	`Room_People`	NUMBER	NOT NULL,
	`Room_Image`	CLOB	NULL,
	`Room_SaleYN`	VARCHAR2(1)	NULL,
	`Room_Memo`	VARCHAR2(1000)	NULL
);

CREATE TABLE `Facility` (
	`Facility_Idx`	NUMBER	NOT NULL,
	`Admin_Idx`	NUMBER	NOT NULL,
	`Facility_name`	VARCHAR2(200)	NULL,
	`Facility_info`	VARCHAR2(1000)	NULL,
	`Facility_image`	CLOB	NULL,
	`Facility_memo`	VARCHAR2(1000)	NULL
);

CREATE TABLE `RESERVATION` (
	`Resv_num`	NUMBER	NOT NULL,
	`AdminIdx`	NUMBER	NOT NULL,
	`UserMail`	VARCHAR2(100)	NOT NULL,
	`Resv_Price`	NUMBER	NULL,
	`Resv_People`	NUMBER	NULL,
	`Resv_Status`	VARCHAR2(20)	NULL,
	`Resv_PayStatus`	VARCHAR2(20)	NULL,
	`Resv_PayMethod`	VARCHAR2(20)	NULL,
	`Resv_ArrivalTime`	VARCHAR2(20)	NULL,
	`Resv_Request`	VARCHAR2(1000)	NULL,
	`Resv_Day`	Date	NULL,
	`Resv_Order_Id`	VARCHAR2(64)	NULL
);

CREATE TABLE `INQUIRY` (
	`Inquiry_Idx`	NUMBER	NOT NULL,
	`Admin_Idx`	NUMBER	NOT NULL,
	`User_Mail`	VARCHAR2(100)	NOT NULL,
	`Inquiry_Title`	VARCHAR2(200)	NOT NULL,
	`Inquiry_Content`	VARCHAR2(2000)	NULL,
	`Inquiry_Answer_Content`	VARCHAR2(2000)	NULL,
	`Inquiry_Status`	VARCHAR2(20)	NOT NULL,
	`Inquiry_Created_At`	DATE	NOT NULL
);

CREATE TABLE `ADMIN_REQUEST` (
	`Request_Idx`	NUMBER	NOT NULL,
	`Ryokan_Name`	VARCHAR2(100)	NULL,
	`Applicant_Name`	VARCHAR2(50)	NULL,
	`Applicant_Email`	VARCHAR2(100)	NULL,
	`Applicant_Tel`	VARCHAR2(50)	NULL,
	`Request_Message`	VARCHAR2(1000)	NULL,
	`Request_Status`	VARCHAR2(20)	NULL,
	`Request_At`	DATE	NULL,
	`Processed_At`	DATE	NULL,
	`Admin_Idx`	NUMBER	NULL
);

CREATE TABLE `RESTAURANT_COURSE` (
	`Restaurant_Course_Idx`	NUMBER	NOT NULL,
	`Restaurant_Course_Name`	VARCHAR2(100)	NULL,
	`Restaurant_Course_Image`	CLOB	NULL,
	`Restaurant_Course_Info`	VARCHAR2(1000)	NULL,
	`Admin_Idx`	NUMBER	NOT NULL,
	`Restaurant_Sale_YN`	VARCHAR2(1)	NULL,
	`Restaurant_Course_Memo`	VARCHAR2(1000)	NULL
);

CREATE TABLE `NOTICE` (
	`Notice_Idx`	NUMBER	NOT NULL,
	`Admin_Idx`	NUMBER	NOT NULL,
	`Notice_Title`	VARCHAR2(200)	NOT NULL,
	`Notice_Content`	VARCHAR2(2000)	NULL,
	`Notice_Created_At`	DATE	NOT NULL
);

CREATE TABLE `RESTAURANT_RESERVATION` (
	`Restaurant_Facility_Idx`	NUMBER	NOT NULL,
	`User_Mail`	VARCHAR2(100)	NOT NULL,
	`Admin_Idx`	NUMBER	NOT NULL,
	`Resv_num`	NUMBER	NOT NULL,
	`Restaurant_Use_Date`	DATE	NULL,
	`Restaurant_Time_Slot`	VARCHAR2(20)	NULL,
	`Restaurant_Headcount`	NUMBER	NULL,
	`Restaurant_Sidemenu`	VARCHAR2(200)	NOT NULL,
	`Course_Idx`	NUMBER	NOT NULL
);

CREATE TABLE `ADMIN` (
	`Admin_Idx`	NUMBER	NOT NULL,
	`Admin_Id`	VARCHAR2(50)	NOT NULL,
	`Admin_Password`	VARCHAR2(200)	NOT NULL,
	`Admin_Name`	VARCHAR2(50)	NULL,
	`Admin_Mail`	VARCHAR2(100)	NULL,
	`Ryokan_Loc`	VARCHAR2(200)	NULL,
	`Ryokan_Name`	VARCHAR2(100)	NOT NULL,
	`Pw_Reset_YN`	VARCHAR2(1)	NULL,
	`Ryokan_Tel`	VARCHAR2(100)	NULL,
	`Ryokan_Access`	VARCHAR2(100)	NULL,
	`Ryokan_Logo`	CLOB	NULL,
	`Ryokan_image`	CLOB	NULL
);

CREATE TABLE `PLAN` (
	`Plan_Idx`	NUMBER	NOT NULL,
	`Admin_Idx`	NUMBER	NOT NULL,
	`Plan_Name`	VARCHAR2(100)	NULL,
	`Plan_Image`	CLOB	NULL,
	`Plan_Includes_Meal`	VARCHAR2(1)	NULL,
	`Plan_Includes_Onsen`	VARCHAR2(1)	NULL,
	`Plan_Info`	VARCHAR2(1000)	NULL,
	`Plan_Price`	NUMBER	NULL,
	`Plan_Sale_YN`	VARCHAR2(1)	NULL
);

CREATE TABLE `ONSEN` (
	`Onsen_Idx`	NUMBER	NOT NULL,
	`Onsen_Name`	VARCHAR2(100)	NULL,
	`Onsen_Image`	CLOB	NULL,
	`Onsen_Info`	VARCHAR2(1000)	NULL,
	`Admin_Idx`	NUMBER	NOT NULL,
	`Onsen_Sale_YN`	VARCHAR2(1)	NULL,
	`Onsen_Memo`	VARCHAR2(1000)	NULL,
	`Onsen_hour`	varchar2(100)	NULL
);

CREATE TABLE `COUNTRY_CODE` (
	`COUNTRY_NAME`	VARCHAR2(50)	NOT NULL,
	`DIAL_CODE`	VARCHAR2(50)	NULL
);

CREATE TABLE `ONSEN_RESERVATION` (
	`Onsen_Facility_Idx`	NUMBER	NOT NULL,
	`Admin_Idx`	NUMBER	NOT NULL,
	`User_Mail`	VARCHAR2(100)	NOT NULL,
	`Resv_num`	NUMBER	NOT NULL,
	`Onsen_Use_Date`	DATE	NULL,
	`Onsen_Time_Slot`	VARCHAR2(20)	NULL,
	`Onsen_Headcount`	NUMBER	NULL,
	`Onsen_Status`	VARCHAR2(20)	NULL,
	`Onsen_Idx`	NUMBER	NOT NULL
);

ALTER TABLE `ROOM_RESERVATION` ADD CONSTRAINT `PK_ROOM_RESERVATION` PRIMARY KEY (
	`Room_Resv_num`
);

ALTER TABLE `MEMBER` ADD CONSTRAINT `PK_MEMBER` PRIMARY KEY (
	`User_Mail`
);

ALTER TABLE `ROOM` ADD CONSTRAINT `PK_ROOM` PRIMARY KEY (
	`Room_Idx`
);

ALTER TABLE `Facility` ADD CONSTRAINT `PK_FACILITY` PRIMARY KEY (
	`Facility_Idx`
);

ALTER TABLE `RESERVATION` ADD CONSTRAINT `PK_RESERVATION` PRIMARY KEY (
	`Resv_num`
);

ALTER TABLE `INQUIRY` ADD CONSTRAINT `PK_INQUIRY` PRIMARY KEY (
	`Inquiry_Idx`
);

ALTER TABLE `ADMIN_REQUEST` ADD CONSTRAINT `PK_ADMIN_REQUEST` PRIMARY KEY (
	`Request_Idx`
);

ALTER TABLE `RESTAURANT_COURSE` ADD CONSTRAINT `PK_RESTAURANT_COURSE` PRIMARY KEY (
	`Restaurant_Course_Idx`
);

ALTER TABLE `NOTICE` ADD CONSTRAINT `PK_NOTICE` PRIMARY KEY (
	`Notice_Idx`
);

ALTER TABLE `RESTAURANT_RESERVATION` ADD CONSTRAINT `PK_RESTAURANT_RESERVATION` PRIMARY KEY (
	`Restaurant_Facility_Idx`
);

ALTER TABLE `ADMIN` ADD CONSTRAINT `PK_ADMIN` PRIMARY KEY (
	`Admin_Idx`
);

ALTER TABLE `PLAN` ADD CONSTRAINT `PK_PLAN` PRIMARY KEY (
	`Plan_Idx`
);

ALTER TABLE `ONSEN` ADD CONSTRAINT `PK_ONSEN` PRIMARY KEY (
	`Onsen_Idx`
);

ALTER TABLE `COUNTRY_CODE` ADD CONSTRAINT `PK_COUNTRY_CODE` PRIMARY KEY (
	`COUNTRY_NAME`
);

ALTER TABLE `ONSEN_RESERVATION` ADD CONSTRAINT `PK_ONSEN_RESERVATION` PRIMARY KEY (
	`Onsen_Facility_Idx`
);
```

### 전체 테이블의 실제 컬럼 확인 쿼리

```sql
SELECT table_name, column_id, column_name, data_type, data_length, nullable
FROM user_tab_columns
WHERE table_name IN (
    'ROOM_RESERVATION','MEMBER','ROOM','FACILITY','RESERVATION','INQUIRY',
    'ADMIN_REQUEST','RESTAURANT_COURSE','NOTICE','RESTAURANT_RESERVATION',
    'ADMIN','PLAN','ONSEN','COUNTRY_CODE','ONSEN_RESERVATION'
)
ORDER BY table_name, column_id;
```

### IDENTITY 자동생성 시퀀스(ISEQ$$_...)가 어느 테이블/컬럼과 연결되는지 확인하는 쿼리

`user_tab_identity_cols`에 IDENTITY 컬럼과 그 컬럼이 쓰는 시퀀스 이름이 직접 나온다 (가장 정확한 방법).

```sql
SELECT table_name, column_name, generation_type, sequence_name
FROM user_tab_identity_cols
ORDER BY table_name;
```

보조 확인용 — 시퀀스 자체의 현재값/증가폭만 보고 싶을 때:

```sql
SELECT sequence_name, min_value, max_value, increment_by, last_number
FROM user_sequences
WHERE sequence_name IN (
    'ISEQ$$_76008','ISEQ$$_76013','ISEQ$$_76017','ISEQ$$_76065','ISEQ$$_76070',
    'ISEQ$$_76039','ISEQ$$_76037','ISEQ$$_76035','ISEQ$$_76033','ISEQ$$_76031',
    'ISEQ$$_76029','ISEQ$$_76025','ISEQ$$_76021'
)
ORDER BY sequence_name;
```

### 실측 결과 (2026-09-16, `user_tab_columns` / `user_tab_identity_cols` / `user_sequences` 실행 결과)

**IDENTITY 시퀀스 ↔ 테이블/컬럼 매핑 (13개 전부 확인됨):**

| 시퀀스 | 테이블 | 컬럼 | last_number |
|---|---|---|---|
| ISEQ$$_76008 | ADMIN | ADMIN_IDX | 21 |
| ISEQ$$_76013 | ROOM | ROOM_IDX | 1 |
| ISEQ$$_76017 | PLAN | PLAN_IDX | 1 |
| ISEQ$$_76021 | ONSEN | ONSEN_IDX | 1 |
| ISEQ$$_76025 | RESTAURANT_COURSE | RESTAURANT_COURSE_IDX | 1 |
| ISEQ$$_76029 | RESERVATION | RESV_NUM | 1 |
| ISEQ$$_76031 | ROOM_RESERVATION | ROOM_RESV_NUM | 1 |
| ISEQ$$_76033 | RESTAURANT_RESERVATION | RESTAURANT_FACILITY_IDX | 1 |
| ISEQ$$_76035 | ONSEN_RESERVATION | ONSEN_FACILITY_IDX | 1 |
| ISEQ$$_76037 | NOTICE | NOTICE_IDX | 1 |
| ISEQ$$_76039 | INQUIRY | INQUIRY_IDX | 4 |
| ISEQ$$_76065 | FACILITY | FACILITY_IDX | 1 |
| ISEQ$$_76070 | ADMIN_REQUEST | REQUEST_IDX | 1 |

(FACILITY/ADMIN_REQUEST엔 애초에 이 프로젝트에서 안 쓰는 것으로 보이는 여분 테이블도 포함 — MEMBER/COUNTRY_CODE는 IDENTITY 컬럼 없음, PK가 자연키(USER_MAIL/COUNTRY_NAME)라 시퀀스 불필요.)

**기존 메모리([[project-ryokanmaker-admin-reservation]])와 다르게 확인된 부분 — 이 실측을 최신 기준으로 삼을 것:**
- `RESTAURANT_COURSE`는 접두어가 없어졌다고 기록되어 있었으나, 실제로는 `RESTAURANT_COURSE_IDX`/`RESTAURANT_COURSE_NAME`/`RESTAURANT_COURSE_IMAGE`/`RESTAURANT_COURSE_INFO`/`RESTAURANT_COURSE_MEMO`/`RESTAURANT_SALE_YN`로 "RESTAURANT_" 접두어가 그대로 있음. `RestaurantCourseDto`가 접두어를 뗀 채로 남아있다면 다시 확인 필요.
- `ADMIN`에 `RYOKAN_IMAGE`(CLOB, nullable) 컬럼이 추가로 존재 — `AdminDto`에 반영 여부 확인 필요.
- `RESERVATION.RESV_PRICE`/`RESV_PEOPLE`/`RESV_STATUS`가 실측상 NOT NULL (기존 ERD엔 NULL 허용으로 표기).

**후속 처리 (2026-09-16):**
- `ADMIN.RYOKAN_IMAGE`(CLOB): 료칸 홈페이지에 띄우는 로고/대표 이미지 컬럼으로 확인. `AdminDto`에 아직 반영 안 됨 — A담당(정보등록 화면) 작업 시 반영 필요.
- `RestaurantCourseDto` 필드명을 실제 DB 컬럼과 맞춰 수정 완료: `courseIdx`→`restaurantCourseIdx`, `courseName`→`restaurantCourseName`, `courseImage`→`restaurantCourseImage`, `courseInfo`→`restaurantCourseInfo`, `courseSaleYN`→`restaurantSaleYN`, `courseMemo`→`restaurantCourseMemo`. 이 DTO는 아직 Mapper/Service/Controller 어디서도 참조되지 않아 컴파일 영향 없음.
