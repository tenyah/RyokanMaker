# 작업 기록

## 메일 계정을 application.properties에 평문으로 복원 (2026-09-22, 미커밋)

**증상:** 회원가입 시 `MailAuthenticationException: failed to connect, no password specified?` — 가입은 정상, 가입 완료 메일만 실패. 원인은 Choiyeongsu13이 바꿔둔 `spring.mail.username/password=${MAIL_USERNAME:}/${MAIL_PASSWORD:}`인데 이 PC엔 환경변수가 없어 둘 다 빈 값. `EmailService.send()`는 계정이 비었는지 확인하지 않고 Gmail 접속을 시도하므로 **"조용히 건너뜀"이 아니라 매번 WARN + 긴 스택을 남긴다**(앞 항목의 "조용히 건너뜀" 서술은 틀림).

**조치:** 사용자 지시로 이전에 쓰던 설정(Gmail 주소 + 16자리 앱 비밀번호, smtp auth/starttls)을 `application.properties`에 **평문으로** 넣음. 환경변수 방식과 "커밋하지 말라"는 팀원 주석은 제거됨.

**⚠️ 주의:** 저장소가 공개라 **이 파일을 push하면 앱 비밀번호가 다시 공개된다**(이미 과거 커밋 `3382654`에도 있음). 사용자는 시연 후 비밀번호를 폐기할 계획. 또 팀원들은 환경변수 방식을 쓰고 있어, push하면 팀원 로컬 설정에도 영향. Eclipse 서버를 재시작해야 반영됨.

---

## fetch 설정 수정 + yeseong `dc035a5`(환율 적용, 플랜 사진) 병합 — 실습 폴더 제외 (2026-09-22)

**fetch 설정:** 사용자 승인으로 `remote.origin.fetch`를 `+refs/heads/june47087-byte:...`(단일 브랜치) → **`+refs/heads/*:refs/remotes/origin/*`**로 변경. 이제 `git fetch`만으로 팀원 브랜치가 모두 갱신된다(앞 항목들의 "fetch가 june47087-byte만 받는다" 주의사항은 해소됨).

**yeseong `dc035a5` "환율적용, 플랜 사진 출력"(9/22 15:16):** 54개 파일 중 **43개는 RyokanMaker와 무관한 수업 실습 프로젝트**(`exMaven/`, `exMyBatisGradle/`, `exGradle/` + "스프링부트 실습용" 최상위 `README.md`). 사용자 지시로 이 3개 폴더와 README는 병합에서 제외(`git rm -r -f`, 병합 전 로컬에 같은 폴더가 없음을 확인). 우리 브랜치 최상위는 계속 `RyokanMaker/` 하나.
- 실제 변경(11파일): `ExchangeRateService`(frankfurter.dev에서 JPY→KRW/USD 환율, 기동 시 `@PostConstruct` + 매일 03시 `@Scheduled`), `PriceDisplayHelper`, `RyokanMakerApplication`에 `@EnableScheduling`, `PaymentController`(DB 가격은 **엔화 기준**, 결제는 원화라 결제 금액을 원화로 환산), 메시지, `planSelect`/`reservation`/`rooms`/`plan_sales` 화면.
- 충돌 4개:
  - messages 3종: yeseong이 `adm.ir_onsen_price`/`adm.ir_course_price`/`adm.ir_price_ph`를 "(엔)" 문구로 바꿨는데, 우리 파일에선 이 키들이 앞선 eartth21 병합 때 **파일 끝으로 옮겨져 있어** 그대로 받으면 키 중복. → 충돌 블록은 버리고 **끝에 있는 기존 키의 값만 yeseong 문구로 교체**. 3개 언어 678키, 중복 0.
  - `plan_sales.html`: 플랜 가격 `₩`→`¥`(DB가 엔화 기준)은 받고, 저쪽 줄에 남아 있던 연필 버튼(우리가 이전에 제거)은 버림.
- `PaymentController`는 자동 병합 — yeseong의 환율 변환과 이쪽의 회원 정보 자동 입력·예약자 검증·RESV_MAIL 메일이 모두 유지됨.
- 검증: compile BUILD SUCCESS, 18080 기동 성공 + 환율 갱신 로그(`1엔 = 8.7251원`) + 공개 화면 3개 200 확인 후 종료.
- **참고:** `RESV_PRICE`/`ROOM_RESERVATION.RESV_PRICE`는 환산된 **원화** 결제액이 저장되고, 관리자 화면의 객실·플랜 가격(`ROOM_PRICE`/`PLAN_PRICE`)은 **엔화**다. 관리자 예약 목록의 `₩` 표시는 결제액이라 맞음.

---

## 예약자 정보를 RESERVATION에 저장 + 관리자 상세에 이름·메일 표시 (2026-09-22, 미커밋)

**배경:** 사용자가 DB의 RESERVATION에 예약자 컬럼 5개를 직접 추가(익스포트 `C:/Users/june3/Ryokan3.sql` 351행). 라이브 DB(`user_tab_columns`, 읽기 전용)에서도 동일 확인:
- `RESV_FIRST_NAME_EN` VARCHAR2(50) **NOT NULL**, `RESV_LAST_NAME_EN` VARCHAR2(50) **NOT NULL**, `RESV_LAST_NAME_JP` VARCHAR2(50) NULL, `RESV_FIRST_NAME_JP` VARCHAR2(50) NULL, `RESV_MAIL` VARCHAR2(100) **NOT NULL**.
- 전화번호·국가 컬럼은 추가하지 않음 → 결제 화면의 전화/국가 입력은 여전히 저장되지 않고, 관리자 상세의 연락처·국적은 **예약한 회원(MEMBER)** 값.
- ⚠️ NOT NULL 3개를 기존 INSERT가 채우지 않아 **컬럼 추가 직후부터 코드 수정 전까지 새 예약 저장이 전부 실패(ORA-01400)하는 상태였음.**

**구현:**
- `ReservationDto`에 `resvLastNameEn/resvFirstNameEn/resvLastNameJp/resvFirstNameJp/resvMail`.
- `PaymentMapper.insertReservation`에 5개 컬럼 추가(일본어 이름은 `jdbcType=VARCHAR`).
- `PaymentReservationService.saveAsWaiting`: 결제 화면 폼(`GuestInfoForm`)의 영문/일본어 성·이름, 이메일을 앞뒤 공백 제거 후 저장(빈 값은 null). **예약한 회원 계정은 기존처럼 `USER_MAIL`(MEMBER FK)에 따로 남음.**
- `PaymentController.prepare`: 영문 성·이름·이메일이 비면 DB 오류 대신 400 `missing guest info` → 화면에 `pay.alert_guest_required` 안내(브라우저 `required`가 먼저 막지만 서버에서 한 번 더).
- 관리자 상세: `selectReservationHeader`에 5개 컬럼, `AdminReservationDetailDto`·서비스에 반영. `admin_reservation.html` 예약자 정보에 **영문 이름 / 일본어 이름** 행 추가(없으면 `-`), **이메일은 `RESV_MAIL`**(사용자 요청). 헤더의 닉네임은 예약한 회원 것 그대로.
- 목록 검색: 회원 이름·메일에 더해 **`RESV_MAIL`·예약자 영문/일본어 이름**도 검색되도록 확장(다른 분 이름으로 한 예약도 찾히게).
- 메시지 ko/en/ja: `adm.rv_name_en`, `adm.rv_name_jp`, `pay.alert_guest_required`. 678키, 중복 0.

**검증:** `mvnw compile` BUILD SUCCESS, RESERVATION INSERT는 `PaymentMapper` 한 곳뿐임을 확인. **실제 예약→관리자 상세 표시는 미확인.**

**(후속) 전화·국가 컬럼 추가 + 예약 완료 메일 수신자 변경:** 사용자가 `RESV_COUNTRY` VARCHAR2(50) **NOT NULL**, `RESV_TEL` VARCHAR2(20) **NOT NULL** 추가(익스포트 `C:/Users/june3/Ryokan4.sql` 369-370행, 라이브 DB에서도 확인). 이번에도 NOT NULL이라 **코드 반영 전까지 새 예약 저장이 실패하던 상태였음.**
- 저장: `RESV_COUNTRY`는 결제 화면 **국가 코드(KR/JP/US/CN/TW/ETC)** 그대로, `RESV_TEL`은 입력값(공백 제거).
- `prepare` 검증 확장: 국가·전화 필수 추가 + **Oracle 바이트 길이 초과 검사**(영문 이름 50, 일본어 이름 50, 메일 100, 국가 50, 전화 20 — UTF-8 바이트 기준). 응답 코드를 `invalid guest info`로 통일, 안내 문구(`pay.alert_guest_required`)도 필수·길이 설명으로 갱신. 입력창에 `maxlength`(영문 50, **일본어 16 = 50바이트/가나 3바이트**, 메일 100, 전화 20).
- 관리자 상세: 연락처 `RESV_TEL`, 국적 `RESV_COUNTRY`를 `@tr.label('country', 코드)`로 표시(기존 `country.KR` 등 메시지 사용, 코드가 아니면 번역 fallback). **`AdminReservationDetailDto.userTel/userCountry`와 MEMBER에서 채우던 코드는 제거**(닉네임만 MEMBER에서).
- **예약 완료 메일 → `RESV_MAIL`**: `sendReservationMail`이 주문번호로 DB의 예약자 정보(`PaymentMapper.selectGuestByOrderId`)를 읽어 발송. 호칭은 `RESV_MAIL`이 로그인 회원 메일과 같으면 닉네임, 다르면 입력한 영문 이름. 로그인 세션이 없어도 발송 가능(예약 컨텍스트만 있으면).
- compile BUILD SUCCESS. **실제 예약·메일 수신은 미확인**(이 PC엔 MAIL_USERNAME/MAIL_PASSWORD 환경변수가 없어 메일은 조용히 건너뜀 — 앞 항목 참고).

---

## 결제 화면 예약자 정보: 회원 정보 자동 입력 · 일본어 이름 선택+가나 전용 · 다른 분 입력 버튼 (2026-09-22, 미커밋)

**사용자 요구:** ① 일본어 이름 필수 아님 ② 일본어 성/이름은 히라가나·가타카나만 ③ 기본값은 로그인 회원 정보 ④ 버튼을 누르면 채워진 정보를 지우고 다른 사람 정보를 직접 입력.

**구현:**
- `PaymentController.payment()`: 세션 `loginMember`(로그인 시 `SELECT *`라 전 컬럼 있음)로 `GuestInfoForm`을 채움 — 일본어/영문 성·이름, 이메일·이메일 확인, 전화번호, 국가. **회원 국가는 COUNTRY_CODE의 국가명(`대한민국` 등)인데 결제 화면 선택지는 코드(`KR`/`JP`/`US`/`CN`/`TW`/`ETC`)라 `countryCodeOf()`로 변환**(목록 밖 국가는 `ETC`).
- 일본어 이름 검증: 브라우저는 입력창 `pattern`, 서버는 `prepare()`에서 `KANA_NAME` 정규식으로 한 번 더(비어 있으면 통과). 범위는 양쪽 동일 — 히라가나 U+3040–309F, 가타카나 U+30A0–30FF(장음 ー 포함), 반각 가타카나 U+FF65–FF9F, 전각·반각 공백. 위반 시 400 `invalid jp name` → 화면에서 가나 안내 alert. Java·브라우저(`v` 플래그)로 たなか/タナカ/ｶﾀｶﾅ/サトー는 통과, 田中/Tanaka/たなか1/한글은 거부 확인.
- `payment.html`: 일본어 두 칸의 `*` 제거, placeholder를 `例) たなか`/`例) たろう`로. 상단에 안내 문구 + **"다른 분 정보로 직접 입력" 버튼** — 텍스트 칸 7개(일본어·영문 성/이름, 이메일 2개, 전화번호)를 비우고 일본어 성에 포커스. 국가 선택(select)은 텍스트 칸이 아니라 그대로 둠.
- **`*` 표시만 있고 실제 검사가 없던 항목에 `required` 추가**(영문 성/이름, 이메일 2개, 국가, 전화번호) — 지금까지는 빈 값으로도 결제가 진행됐고, 지우기 버튼을 넣으면 빈 칸 결제가 더 쉬워지므로.
- 메시지 ko/en/ja: `pay.guest_prefilled`, `pay.guest_other`, `pay.kana_only`. 676키, 중복 0.
- ⚠️ Edit 도구가 `pattern`의 `\u3040` 이스케이프를 실제 유니코드 문자(보이지 않는 전각 공백 포함)로 바꿔 써서, 파이썬으로 이스케이프 표기로 되돌림. 이 파일의 `pattern`을 고칠 땐 바이트(`od -c`)로 확인할 것.

**검증:** `mvnw compile` BUILD SUCCESS(Eclipse 서버가 쓰는 target을 지우지 않도록 clean 없이). **화면 동작은 미확인.**

**(후속) "이메일 주소 확인" 칸 삭제:** 이메일과 일치하는지 비교하는 코드도, 저장하는 곳도 없어서 사용자 요청으로 제거. `payment.html`의 칸, "다른 분 입력" 버튼의 비우기 목록, `PaymentController`의 자동 입력 줄, `GuestInfoForm.emailConfirm` 필드, 메시지 `pay.email_confirm`(ko/en/ja)까지 정리. compile BUILD SUCCESS.

**(후속) 예약자 정보 저장 위치 확인:** 사용자는 예약자 정보가 MEMBER가 아니라 RESERVATION으로 옮겨졌다고 알고 있었으나, **실제 DB(`user_tab_columns`, 읽기 전용 조회)의 RESERVATION에는 이름·이메일·전화·국가 컬럼이 없음** — RESV_NUM, ADMIN_IDX, USER_MAIL, RESV_PRICE, RESV_PEOPLE, RESV_STATUS, RESV_PAY_STATUS, RESV_PAY_METHOD, RESV_ARRIVAL_TIME, RESV_REQUEST, RESV_DAY, RESV_ORDER_ID 뿐. 이름 등은 여전히 MEMBER에 있음. 코드·전 브랜치 히스토리에도 추가 흔적 없음. 옮기려면 ALTER TABLE(사용자가 직접 실행) + prepare 저장 + 관리자 상세 표시가 필요 — **→ 이후 사용자가 컬럼을 직접 추가해 해결(맨 위 "예약자 정보를 RESERVATION에 저장" 참고).**


**참고(기존 구조):** 결제 화면의 예약자 이름·이메일·전화번호는 **DB에 저장되지 않는다** — `prepare()`는 폼을 세션에만 두고, RESERVATION에는 도착 시간·요청 사항만 들어가며 예약자는 로그인 회원 이메일(`USER_MAIL`, MEMBER FK)로 기록된다. 예약 완료 메일도 로그인 회원에게 간다. 즉 "다른 분 정보"는 지금 구조에선 결제 화면에서만 쓰이고 관리자 예약 상세엔 나오지 않음.

---

## 관리자 예약현황 상세: 예약 취소 구현 — DB 상태만 변경, 토스 환불 없음 (2026-09-22, 미커밋)

**⚠️ 최종 방식 (아래 초기 구현에서 변경됨):** 토스 환불을 호출하지 않고 **DB 상태만 바꾼다.** 예약·객실 예약 `RESV_STATUS='예약취소'`, 온천 `ONSEN_STATUS='예약취소'`, 결제완료였던 건은 `RESV_PAY_STATUS='결제취소'`(결제대기는 그대로). `TossPaymentService.cancelByOrderId`와 `PAY_STATUS_REFUNDED`/`resv.status.환불완료`/`adm.rv_cancel_confirm_refund`는 삭제, `PAY_STATUS_CANCELLED="결제취소"`·`resv.status.결제취소`(ko/en/ja) 추가, 확인창 문구는 하나로 통일. 메시지 673키, 중복 0.

**변경 이유 — 토스 환불이 불가능했음:** 첫 구현은 주문번호로 결제를 조회(`GET /v1/payments/orders/{orderId}`)해 paymentKey를 얻고 취소하는 방식이었는데, 실제 취소 시 `NOT_FOUND_MERCHANT(존재하지 않는 상점 정보)`로 실패. 원인은 **`application.properties`의 토스 키가 문서 공개용 샘플 키(`test_gck_docs_…`/`test_gsk_docs_…`)**라는 것. 없는 주문번호로 직접 호출해도 같은 `NOT_FOUND_MERCHANT`가 나와 **샘플 키로는 주문번호 조회 API 자체를 못 쓴다**는 걸 확인(반면 paymentKey로 조회·취소 API는 `NOT_FOUND_PAYMENT`를 돌려줘 호출 자체는 가능). 샘플 키 결제는 토스 공용 테스트 상점에 기록돼 **개발자센터에서 수동 취소할 방법도 없음.** 테스트 결제라 실제 출금은 없으므로 사용자 결정으로 화면상 취소만 하기로 함.

**나중에 실제 환불이 필요해지면:** ① 결제 승인 시 paymentKey를 RESERVATION에 새 컬럼으로 저장 → `POST /v1/payments/{paymentKey}/cancel` 직접 호출(키 종류와 무관하게 동작), 또는 ② 토스 개발자센터에서 우리 상점 전용 테스트 키를 발급받아 교체. 어느 쪽이든 **이미 샘플 키로 결제된 예약은 환불 불가.**

**(초기 구현 기록 — 아래 중 토스 환불 관련 내용은 위 최종 방식으로 대체됨)**

**사용자 결정:** ① 취소 시 DB에서 지우지 않고 **상태만 `예약취소`로** 바꿔 기록 유지 ② **결제완료 건은 토스 결제 취소(환불)까지** 호출.

**구현:**
- `TossPaymentService.cancelByOrderId(orderId, reason)` 신규 — DB에 `paymentKey`를 저장하지 않으므로 `GET /v1/payments/orders/{orderId}`로 결제를 조회해 paymentKey를 얻고 `POST /v1/payments/{paymentKey}/cancel`(전액, Idempotency-Key 포함) 호출. 인증 헤더 생성은 `basicAuth()`로 confirm과 공용화.
- `AdminReservationService.cancelReservation(resvNum, adminIdx)` (`@Transactional`):
  - 예약이 없거나 **다른 관리자 예약이면 거부**, 이미 `예약취소`면 거부.
  - `결제완료`면 **토스 환불을 먼저** 호출 → 실패 시 예외로 끝나 DB는 그대로. 성공하면 결제상태를 `환불완료`로. `결제대기`는 환불 없이 상태만 취소(결제상태는 결제대기 유지).
  - RESERVATION·ROOM_RESERVATION의 `RESV_STATUS='예약취소'` + 결제상태, ONSEN_RESERVATION의 `ONSEN_STATUS='예약취소'`. **RESTAURANT_RESERVATION은 상태 컬럼이 없어 그대로 둠**(예약 본체 상태로 판단).
  - (한계) 토스 환불 성공 후 DB 갱신이 실패하면 환불만 되고 DB는 롤백됨 — 드문 경우라 로그로 확인.
- 상수: `PaymentReservationService.STATUS_CANCELLED="예약취소"`, `PAY_STATUS_REFUNDED="환불완료"`.
- `AdminController`: `POST /Admin/reservation_cancel`(resvNum, page, checkInDate, keyword) → 처리 후 **같은 예약·페이지·필터로 리다이렉트**(URLEncoder로 인코딩), 결과는 flash `cancelResult=success|fail`.
- `admin_reservation.html`: 동작 없던 버튼을 POST 폼으로. `confirm()` 확인창(결제완료 건은 "전액 환불됩니다" 문구). 이미 취소된 예약이면 버튼 대신 "취소된 예약입니다". 처리 결과 메시지 표시.
- **취소건이 객실을 계속 점유하지 않도록 `RESV_STATUS != '예약취소'` 조건 추가:**
  - `RoomReservationMapper.findOverlapping` — **손님 예약 화면의 객실 가능 여부**, 객실 현황 캘린더·당일 객실 관리, 판매관리, 대시보드 가동률이 전부 이걸 씀.
  - `RoomReservationMapper.findByAdminAndCheckInRange`(대시보드 매출), `RevenueMapper`의 매출 거래·오늘 체크인·오늘 체크아웃 3개, `AdminReservationMapper.countTodayCheckIns/Outs`.
  - 관리자 예약 목록·회원 마이페이지 목록은 **취소건도 계속 보여줌**(기록 유지 목적, 상태 라벨 `예약취소`).
- 메시지 ko/en/ja: `resv.status.환불완료`, `adm.rv_cancel_confirm`, `adm.rv_cancel_confirm_refund`, `adm.rv_cancel_done`, `adm.rv_cancel_fail`, `adm.rv_cancelled_note` 추가(`resv.status.예약취소`는 기존에 있었음). 3개 언어 674키, 중복 0.

**검증:** `clean compile` BUILD SUCCESS, 18080 기동으로 매퍼 파싱 정상 + `POST /Admin/reservation_cancel` 매핑 확인(비로그인 302). **로그인 후 실제 취소·토스 테스트 환불·객실 재오픈은 미확인.** 확인 순서: 결제완료 예약 취소 → 토스 개발자센터에서 해당 결제가 취소됐는지 → 상세에 "취소된 예약입니다" + 결제상태 환불완료 → 같은 날짜로 손님 예약 화면에서 그 객실이 다시 예약 가능인지 → 오늘 체크인 숫자 감소.

---

## 예약 시 식사·온천 예약을 DB에 저장 — 관리자 예약현황 상세에 식사/온천이 안 뜨던 문제 (2026-09-22, 미커밋)

**증상:** `admin_reservation.html` 상세 패널에 객실만 나오고 식사·온천 예약이 비어 있음.

**원인:** 화면/조회 쿼리 문제가 아니라 **데이터가 애초에 저장되지 않았다.** `RESTAURANT_RESERVATION` / `ONSEN_RESERVATION`에 INSERT하는 코드가 프로젝트 어디에도 없었음(조회는 `AdminReservationMapper`, 삭제는 회원탈퇴 `ReservationMapper`뿐). 결제 준비(`PaymentReservationService.saveAsWaiting`)가 RESERVATION·ROOM_RESERVATION만 저장했기 때문. 9/18 기록의 "온천/식사 선택값은 ReservationContext에 담기만 하고 테이블엔 저장 안 함"이 그대로 남아 있던 것.

**구현:**
- `PaymentMapper(.java/.xml)`: `insertRestaurantReservation`, `insertOnsenReservation` 추가. 두 테이블 PK(`RESTAURANT_FACILITY_IDX`/`ONSEN_FACILITY_IDX`)는 IDENTITY(9/16 실측)라 컬럼 목록에서 뺌. 비어 있을 수 있는 `timeSlot`/`headcount`에 `jdbcType` 지정(ORA-17004 재발 방지).
- `saveAsWaiting()`(이미 `@Transactional`) — ROOM_RESERVATION 저장 직후:
  - **식사:** `courseIdx`가 있으면 **숙박하는 밤마다 1건**(체크인일 ~ 체크아웃 전날). 코스 요금이 "1박 기준, 숙박 일수만큼 적용"(`resv.course_per_night_note`)이라 그에 맞춤. 인원 = 예약 인원. **식사 시간대는 예약 화면에서 받지 않아 NULL.** `RESTAURANT_SIDEMENU`가 NOT NULL인데 입력 UI가 없어 **`"-"`로 채움**(`NO_SIDEMENU` 상수).
  - **온천:** eartth21/yeseong이 만든 `ReservationContext.onsenPicks`(날짜·온천·시간대 목록, `ReservationService.parseOnsenPicks`)를 **선택 1건당 1행**으로 저장. 상태 `예약완료`, 인원 = 예약 인원. `onsenIdx`가 없는 항목은 건너뜀.
- `admin_reservation.html`: 식사·온천 상세 문구의 시간대가 null이면 `"null 타임"` 대신 `"- 타임"`으로 표시(`?: '-'`).

**검증:** `clean compile` BUILD SUCCESS, 포트 18080으로 기동해 매퍼 XML 파싱 정상 확인 후 종료. **실제 결제 골든패스(예약 → 결제 → 관리자 상세)로 행이 생기는지는 미확인.**

**주의:**
- **이미 만들어진 기존 예약에는 식사·온천 행이 없으므로 계속 안 뜬다.** 이번 수정 이후 새로 한 예약부터 표시됨.
- 결제창을 띄웠다 취소하면 객실처럼 식사·온천 행도 `결제대기` 예약에 딸린 채 남는다(기존 트레이드오프와 동일).
- 식사 시간대·사이드메뉴를 실제로 받으려면 예약 화면에 입력 칸을 추가해야 함 — 필요 시 사용자와 논의.

---

## Choiyeongsu13 병합 + dto→domain 재통일 + Lombok 전환 (2026-09-22)

**⚠️ 먼저 알아둘 것: `remote.origin.fetch`가 `+refs/heads/june47087-byte:refs/remotes/origin/june47087-byte` 하나만 추적하도록 설정돼 있다.** 그래서 `git fetch origin`을 해도 다른 브랜치의 원격 추적 정보가 갱신되지 않는다. 이번 세션 초반에 이걸 모르고 "Choiyeongsu13/eartth21은 고유 커밋 0개"라고 잘못 판단했다. **팀원 브랜치를 볼 때는 반드시 `git ls-remote origin`으로 실제 상태를 확인하거나 `git fetch origin '+refs/heads/*:refs/remotes/origin/*'`로 받을 것.** (config는 사용자 확인 없이 안 고쳤음 — 고칠지 논의 필요.)

**파악한 실제 구조:** Choiyeongsu13이 사실상 팀 통합 브랜치다. 9/19에 `integrate-0919`에서 june47087-byte·eartth21·yeseong을 전부 병합해뒀다(`fa9f711`/`891dc87`/`a4a4a4d`). 그래서 남은 차이는 우리 최신 4커밋뿐이었고, eartth21 고유는 `1032d84 대시보드 매출현` 1개, yeseong 고유는 `381d1d0 예약페이지 시간 추가` 1개. origin/master는 `0916 은예성`을 넣었다 Revert해 실질 변경 0.

**병합 (`aa08be4`, 156파일 +5190/-2588):** 가져온 것 — 다국어 KO/EN/JA 전체 적용(고정 문구는 messages, DB 콘텐츠는 Gemini 자동 번역), PAGE_CONTENT(관리자가 손님 화면 문구 편집), 교통안내 `/access` 페이지, 회원 비밀번호 찾기, `admin_shell.html` 프래그먼트, rooms/onsen/dining/facility 페이지.

**충돌 9개 해소 원칙 — 관리자 화면은 우리 레이아웃 + 저쪽 i18n 키:**
- `room_status.html`(충돌 11곳), `plan_sales.html`(5곳), `admin_reservation.html`(3곳), `admin_inquiry.html`(1곳): 저쪽은 **옛 레이아웃에 다국어 키만 입힌 상태**였고 우리는 같은 기간에 그 화면들을 새로 짰다. 우리 구조(등록 폼 제거, 당일 객실 관리, 온천 판매 관리, 목록 페이지 넘김)를 살리고 메시지 키를 다시 입혔다.
- **등록 폼이 화면에서 빠졌으므로** `adm.rs_empty`/`adm.ps_empty`의 "아래에서 새 ○○을 추가해 주세요"를 "정보 등록 화면에서"로 3개 언어 모두 수정. `adm.rs_desc`도 당일 관리 내용으로 교체.
- 새 키 12개를 ko/en/ja에 추가: `adm.checked_in`, `adm.rs_broken`, `adm.rs_broken_short`, `adm.rs_today_manage`, `adm.rs_today_rooms`, `adm.rs_booked_before_in`, `adm.rs_no_today_resv`, `adm.rs_checkin_mark`, `adm.rs_checkin_mark_title`, `adm.rs_checkin_cancel`, `adm.ps_onsen_sale`, `adm.ps_onsen_empty`.
- `PageIndex.java`: 저쪽이 `ad44d9d 미사용 DTO/유틸 클래스 정리`에서 지웠으나(당시엔 실제로 아무도 안 썼음) 지금은 페이지 넘김이 쓰므로 **우리 버전 복구**.
- `common.css`/`EmailService`/`WORKLOG`: 양쪽 내용 모두 보존.
- `PaymentController`: 양쪽 필드(AdminMapper·EmailService ↔ MessageSource·GeminiTranslationService) 전부 유지.

**⚠️ 삽질 기록 — Lombok이 깨진 줄 알았던 건:** 병합 직후 컴파일하면 `cannot find symbol: getXxx()` 에러가 DTO 전반에 수백 개 쏟아져서 Lombok 애노테이션 처리가 안 되는 것처럼 보였다. **실제 원인은 `MemberService`에 `emailService` 필드가 양쪽에서 각각 추가돼 중복 선언(`variable emailService is already defined`)된 것 하나뿐이었다.** 애노테이션 처리 라운드에서 에러가 나면 Lombok 생성 메서드가 전부 없는 것처럼 보이는 전형적인 증상. **에러 목록은 정렬하지 말고 맨 앞부터 볼 것** — 첫 줄이 진짜 원인이었다. 중복 필드 제거 후 바로 BUILD SUCCESS.

**dto→domain 재통일:** 우리가 9/18에 `dto`→`domain`으로 바꿨었는데(`9978e5e`), Choiyeongsu13이 9/19 병합에서 `dto`를 유지하는 쪽으로 해소해 사실상 되돌려놨다. 이번 병합으로 트리가 다시 `dto`가 됐고, 사용자 지시로 **`domain`으로 재통일**했다. `git mv`로 폴더를 옮기고 `sed`로 89개 파일(java+xml)의 `ryokanmaker.dto`→`ryokanmaker.domain` 일괄 치환. `application.properties`에 `type-aliases-package` 설정은 없어서 건드릴 것 없었음.
- **주의:** 팀원 3명 브랜치는 전부 `dto`다. 다음 병합에서 같은 충돌이 또 난다 — 팀과 패키지명을 합의하는 게 근본 해결.

**Lombok 점검:** DTO 30개 중 28개는 이미 `@Data` 계열이 붙어 있었고, `AdminRequestDto`(수동 getter/setter 20개)와 `CountryCodeDto`(4개)만 순수 POJO여서 Lombok으로 전환. `AdminRequestDto`는 무인자 생성자만 있었으므로 `@Data @NoArgsConstructor`, `CountryCodeDto`는 2인자 생성자도 쓰이므로 `@Data @NoArgsConstructor @AllArgsConstructor`. pom.xml에 lombok 의존성과 `annotationProcessorPaths` 설정은 이미 정상이었다.

**검증:** `mvnw clean compile` + `test-compile` BUILD SUCCESS. **서버 기동/화면 확인은 아직 안 함.** 다국어 전환(KO/EN/JA)에서 방금 손댄 관리자 화면 4개가 제대로 나오는지, 특히 새로 추가한 키 12개와 페이지 넘김이 3개 언어에서 다 보이는지 확인 필요.

**→ 팀 합의로 `domain` 확정 (2026-09-22).** 앞으로 팀원 브랜치를 병합할 때 `dto`로 들어오는 파일은 전부 `domain`으로 옮기고 참조를 치환할 것.

---

## eartth21 · yeseong 병합 완료 + push (2026-09-22) — 팀원 브랜치 전부 통합됨

**결과:** Choiyeongsu13 / eartth21 / yeseong / Test 모두 `HEAD..origin/<branch>` = 0. `origin/june47087-byte`에 push 완료(`300dcfe..254d17a`, 40커밋). origin/master의 2커밋은 `0916 은예성` 추가 후 바로 Revert한 실질 변경 0이라 제외.

**eartth21 (`0535e17`):** `1032d84 대시보드 매출현` — 대시보드(`/Admin/dashboard`), 월/일자별 매출(`/Admin/revenue_monthly`, `/Admin/revenue_daily`), 예약 화면 가격 세부 표시. 새 DTO 12개는 git이 rename을 따라 `domain/`에 자동 배치했고 package 선언과 새 컨트롤러·서비스·매퍼(22개 파일)의 참조를 `domain`으로 치환. 실제 충돌 7개: PaymentController(import·필드 합집합), RoomReservationMapper.java/.xml(양쪽이 다른 메서드 추가 → 둘 다 유지), ReservationService(import, eartth21 쪽이 상위집합), messages 3종(파일 끝 양쪽 추가 → 둘 다 유지). **메시지 키는 3개 언어 모두 667개로 일치, 중복 0** (adf5419에서 팀원이 막 고친 중복 키 버그 재발 방지 차원에서 확인).

**yeseong (`254d17a`) — `-s ours`로 기록 + 고유분만 반영:**
- `381d1d0`은 오래된 `e3e277e`(9/18) 위에 9/21 시점 프로젝트 전체를 통째로 붙여넣은 커밋이라 그냥 병합하면 **충돌 96개**. 팀 커밋들과 트리 diff를 비교해 **`104a4b1`(관리자 화면 다국어 적용) 스냅샷을 복사해 간 것**으로 판단(diff 479줄로 최소).
- `104a4b1 → 381d1d0` 실제 변경은 23개 파일. **그중 17개는 eartth21 `1032d84`와 blob이 완전히 동일**(jiji가 yeseong 작업을 가져가 그 위에 대시보드를 얹은 것). 나머지 6개 중 messages 3종·admin_info_register는 eartth21이 상위집합.
- **yeseong에만 있던 것은 `OnsenService`·`RestaurantCourseService`의 가격 기본값 처리(null·음수면 0) 4줄씩뿐** → 이것만 옮겨옴. 관리자가 가격 칸을 비우고 저장하면 null이 그대로 들어가던 걸 막는다.
- 검증: yeseong이 추가한 모든 줄(dto→domain 치환 후)이 현재 트리에 존재함을 줄 단위로 확인(누락 0).

**서버 기동 확인:** 에러 없이 기동. 공개 화면 9개(`/`, `/reservation/plan`, `/rooms`, `/onsen`, `/dining`, `/facility`, `/access`, `/member/login`, `/member/forgot`) × KO/EN/JA 전부 200. 관리자 화면 9개는 비로그인 시 전부 302 → `/Admin/admin_login`(500 없음). 렌더링된 HTML 30개에서 `??키_ko??` 형태의 **누락 메시지 키 0건**. 요청 처리 중 서버 로그 ERROR 0건. **관리자 로그인 후 화면(특히 병합 충돌을 풀었던 4개 + 대시보드/매출)은 아직 육안 확인 안 함.**

**⚠️ 메일·번역 설정이 환경변수로 바뀜 — 이 PC에선 현재 메일이 안 나간다:**
- Choiyeongsu13이 `application.properties`의 평문 Gmail 계정/앱 비밀번호를 `spring.mail.username=${MAIL_USERNAME:}` / `spring.mail.password=${MAIL_PASSWORD:}`로 바꿨다. 그 밖에 `gemini.api.key=${GEMINI_API_KEY:}`, `mail.secret.key=${MAIL_SECRET_KEY:}` 추가.
- **이 PC에는 네 환경변수가 셸·Windows 사용자·시스템 어디에도 없다.** 기본값이 빈 문자열이라 서버는 뜨지만 → 플랫폼 공용 메일 계정 미설정, Gemini 자동 번역 비활성(EN/JA에서 DB 콘텐츠가 원문 그대로).
- 고객 안내 메일 3종(가입/문의답변/예약완료)은 `sendQuietly`라 **조용히 건너뛰고 경고 로그만 남김** — 화면상 에러가 안 나서 모르고 지나치기 쉬움.
- 메일 발송 구조도 바뀜: `EmailService.resolveSiteSender()`가 료칸 관리자 메일 주소 + DB에 암호화 저장된 앱 비밀번호(`SecretCipher`, 키는 `MAIL_SECRET_KEY`)를 먼저 쓰고, 없으면 플랫폼 공용 계정(`MAIL_USERNAME`/`MAIL_PASSWORD`)으로 보낸다.
- **평문 비밀번호를 properties에 다시 넣지 않았음** — 팀원이 의도적으로 뺀 것이고 공개 저장소라서. 사용자는 시연까지 Gmail 발송이 필요하다고 했으므로 **Eclipse 실행 구성(Run Configurations → Environment) 또는 Windows 사용자 환경변수에 `MAIL_USERNAME`/`MAIL_PASSWORD`를 넣어야 한다.** 번역도 쓰려면 `GEMINI_API_KEY`.

**기타:**
- 이 세션 초반에 띄운 서버를 `TaskStop`으로 껐는데 **mvnw 래퍼만 죽고 자식 java 프로세스(PID 8912)가 8080을 잡은 채 남아 있었다.** 다음 기동 때 "Port 8080 was already in use"로 실패. `Get-NetTCPConnection -LocalPort 8080`으로 PID를 찾아 정리함. 세션 맨 처음 사용자가 물었던 "서버 오류"가 이런 종류였을 가능성 있음 — Eclipse에서 8080 충돌이 나면 이것부터 의심할 것.
- `remote.origin.fetch` refspec은 여전히 `june47087-byte` 하나만 추적 중(사용자 확인 없이 config 변경 안 함).

---

## 관리자 문의 관리 / 예약 현황에 페이지 넘김 추가 (2026-09-22, 미커밋)

**사용자 요구:** `admin_inquiry.html`·`admin_reservation.html`의 목록이 아래로 계속 길어지니 페이지 넘김을 넣을 것. **기존 `util/PageIndex.java`를 사용**하고, 방식은 `C:\Users\june3\git\SpringProject\exSample`(JSP 예제)의 `Board/board_list.jsp` 33~72행 + `BoardController.boardListPage()`를 참고.

**참고한 JSP 패턴:** 컨트롤러에서 `totcount`→`totpage` 계산 후 `PageIndex.pageList(...)`가 만든 HTML 문자열을 `pageSkip`으로 모델에 담고, 화면에서는 `<div align="center">${pageSkip}</div>`로 그대로 출력. Thymeleaf에서는 `th:utext`로 대응.

**구현:**
- `util/PageIndex.java` — 이미 프로젝트에 있었지만 **아무 데서도 안 쓰이던 상태**였음. 기존 `pageList(page,totpage,url,maxlist)`는 4인자 그대로 두고 내부적으로 새 5인자 오버로드에 위임. 5번째 인자 `extraQuery`는 `"&status=답변대기"`처럼 **인코딩까지 끝낸** 추가 쿼리스트링(문의 화면의 상태 필터를 페이지 넘겨도 유지하려고 추가). `<<`/`>>`를 `&lt;&lt;`/`&gt;&gt;`로 이스케이프하고, 현재 페이지와 비활성 화살표를 `<span class='list-now'>`/`<span class='list-off'>`로 감싸 CSS로 강조 가능하게 함. (기존 `pageListHan`은 손대지 않음 — search/key 전용이라 이 화면들과 안 맞음.)
- `AdminController.adminInquiry()` / `reservationStatus()` — `@RequestParam(defaultValue="1") int page` 추가, **페이지당 10건**(`maxlist`). 서비스/매퍼는 그대로 두고 **전체 목록을 받아 `subList`로 자르는 인메모리 페이징**. (DB offset/fetch 페이징이 아님 — 관리자별 목록이라 규모가 작고, 매퍼·쿼리 변경 없이 끝나서 이 방식 선택. 데이터가 커지면 DB 페이징으로 교체 필요.)
- `page`는 `Math.min(Math.max(page,1), totpage)`로 클램프 — URL에 `page=999`나 `page=0`을 넣어도 깨지지 않음.
- **예약 화면 권한 검사는 전체 목록(`allList`) 기준으로 유지** — 페이징된 목록으로 검사하면 다른 페이지의 예약을 클릭했을 때 조회가 안 되므로. 타 관리자 예약번호 차단 효과는 그대로.
- `inquiryAnswer()` 리다이렉트에 `&page=` 추가, 답변 폼에 `<input type="hidden" name="page">` 추가 → 답변 등록 후에도 보던 페이지로 복귀.
- 템플릿 2개: 목록 카드 링크에 `page=${page}` 추가(선택해도 페이지 유지), 목록 아래에 `<div class="page-skip" th:if="${totcount > 0}" th:utext="${pageSkip}">` 추가, 헤더 건수 표기를 `34건 · 1/4` 형태로 변경.
- `admin_reservation.html`의 "전체 예약" 통계 타일이 `#lists.size(reservationList)`를 쓰고 있어 페이징 후엔 페이지 건수(최대 10)만 나오게 됨 → `${totcount}`로 교체.
- `common.css`에 `.page-skip` / `a.list` / `.list-now` / `.list-off` 스타일 추가.

**검증:** `mvnw clean compile` BUILD SUCCESS, `mvnw spring-boot:run` 기동 성공(8080). **화면 렌더링/페이지 이동 동작은 사용자가 직접 확인하기로 함 — 아직 미검증.** 특히 Thymeleaf 식(`th:utext`, `${totcount > 0}`)은 컴파일로 검증되지 않으므로 실제 렌더링 확인 필요. 확인 순서: 문의 11건 이상일 때 `[1] [2]` 노출 → 2페이지 이동 → 카드 클릭 시 2페이지 유지 → 답변 등록 후에도 2페이지 → 상태 필터(답변대기/답변완료) 건 채로 페이지 넘길 때 필터 유지.

**주의:** 문의 화면 상단 필터 링크(전체/답변대기/답변완료)는 `page`를 안 실으므로 필터를 바꾸면 1페이지로 돌아감 — 의도된 동작.

---

## 관리자 예약현황: 오늘 체크인/아웃 실제 집계 + 체크인 날짜 필터(하루) (2026-09-22, 미커밋)

**사용자 요구:** 통계 타일의 "오늘 체크인/체크아웃" 샘플값(4, 3)을 실제 값으로. 필터 바에서 "예약 상태"는 삭제. "체크인 기간"은 **날짜 하루만** 고르게 — 방식은 `reservation/reservation.html` 검색 조건 바의 달력 버튼(= `<input type="date">` 브라우저 기본 캘린더)과 같게, 단 칸은 하나만. **(후속 요청) 검색 칸도 실제 동작하도록 구현** — 아래 "검색 추가" 참고.

**구현:**
- `AdminReservationMapper(.java/.xml)`:
  - `selectReservationList(adminIdx, checkInDate)` — `checkInDate`가 있으면 `EXISTS (ROOM_RESERVATION에서 TRUNC(RESV_CHECK_IN) = checkInDate)` 조건 추가. 목록 카드는 대표 객실(MIN ROOM_RESV_NUM) 1건만 조인하므로, 여러 객실 예약 중 어느 하나라도 그날 체크인이면 걸리도록 EXISTS 사용.
  - `countTodayCheckIns` / `countTodayCheckOuts` 신규 — **`TRUNC(SYSDATE)` 비교(사용자 지정 기준)**. **ROOM_RESERVATION 행 단위, 상태 필터 없음** → eartth21 대시보드(`RevenueMapper.findCheckIns/OutsByAdminAndDate`)와 같은 기준이라 두 화면 숫자가 일치. (대시보드는 `LocalDate.now()`를 쓰므로 서버·DB 시간대가 다르면 어긋날 수 있음 — 로컬에선 같은 PC라 문제없음.)
- `AdminReservationService`: 1인자 `getReservationList`는 컨트롤러 외 사용처가 없어 2인자로 교체, count 메서드 2개 추가.
- `AdminController.reservationStatus()`: `@RequestParam checkInDate`(`@DateTimeFormat ISO.DATE`, 선택) 추가. 모델에 `todayCheckInCount`, `todayCheckOutCount`, `checkInDate`, `allReservationCount`. **"전체 예약" 타일은 날짜 필터와 무관하게 전체 건수**(필터 중이면 무필터 목록을 한 번 더 조회). 목록 헤더 건수(`totcount`)는 필터된 건수. 페이지 넘김 링크에 `&checkInDate=` 유지(`PageIndex` 5인자, ISO 날짜라 인코딩 불필요).
- `admin_reservation.html`: 타일 3개 모두 실제 값. 필터 바의 "예약 상태" 칸 삭제, "체크인 기간" 칸을 GET 폼 + `<input type="date" name="checkInDate" onchange="this.form.submit()">` 하나로 교체(예약 화면과 같은 인라인 스타일). 날짜 선택 즉시 조회, 필터 중엔 옆에 "전체" 링크로 해제. 카드 링크에 `checkInDate` 추가(선택해도 필터 유지). 목록이 비었을 때 안내 문구 추가(필터 유무에 따라 문구 다름).
- 메시지(ko/en/ja): `adm.rv_period` 값을 "체크인 기간"→"체크인 날짜"/"Check-in date"/"チェックイン日"로 변경, `adm.rv_empty`·`adm.rv_empty_date` 추가, 안 쓰게 된 `adm.rv_state` 삭제. 3개 언어 모두 668키, 중복 0.

**검증:** `mvnw clean compile` BUILD SUCCESS. 비로그인 요청 `?checkInDate=2026-09-22` → 302(파라미터 바인딩 오류 없음). **로그인 후 실제 집계값·날짜 필터 동작은 미확인.** 내 서버는 사용자가 Eclipse(STS)로 띄운 서버(11:51 기동)가 8080을 쓰고 있어서 기동 못 함 — 사용자 서버는 끄지 않았다. `clean compile`이 그 서버가 쓰는 `target/classes`를 지웠다 다시 만들었는데 확인 결과 서버는 정상 응답(200).

**검색 추가 (같은 날 후속 요청):**
- `selectReservationList(adminIdx, checkInDate, keyword)` — `keyword`가 있으면 `<bind>`로 `'%'+대문자+'%'`를 만들어 **닉네임 / 이메일 / 영문 이름(이름 성, 성 이름 둘 다) / 일본어 이름(성+이름)** 중 하나라도 부분 일치하면 표시. 영문·이메일은 `UPPER`로 대소문자 무시. 값은 바인드 파라미터라 SQL 인젝션 없음. (`%`·`_`를 입력하면 와일드카드로 동작 — 이스케이프는 안 함.)
- 컨트롤러: `keyword`는 앞뒤 공백 제거, 빈 문자열이면 null. `filtered = checkInDate != null || keyword != null`. "전체 예약" 타일은 필터 중이면 무필터로 한 번 더 조회해 전체 건수 표시. 페이지 링크에 `&keyword=`(URLEncoder로 인코딩 — `PageIndex`가 `th:utext`로 출력되므로 인코딩이 곧 XSS 방어이기도 함).
- 화면: 필터 바 전체를 하나의 GET `<form class="search-bar">`로 묶어 **날짜와 검색어가 함께 전송**됨. 날짜는 바꾸면 즉시 조회, 검색어는 Enter 또는 돋보기 버튼. 필터가 하나라도 걸려 있으면 "전체" 링크로 모두 해제. 카드 링크에 `keyword`도 유지.
- 메시지: `adm.rv_empty_date`를 날짜·검색 공통 문구 `adm.rv_empty_filtered`("검색 조건에 맞는 예약이 없습니다" / en / ja)로 교체. 3개 언어 668키, 중복 0.
- 검증: `clean compile` BUILD SUCCESS, **포트 18080으로 잠깐 기동**해 MyBatis 매퍼 XML(`<bind>` 포함) 파싱 정상 + `?checkInDate=&keyword=(한글)&page=2` 바인딩 오류 없음(비로그인 302) 확인 후 종료. 8080은 사용자 Eclipse용으로 비워 둠. **로그인 후 실제 검색 결과는 미확인.**

**참고:** 결제창을 띄웠다 취소한 `결제대기` 예약도 "오늘 체크인" 집계와 목록에 포함된다(대시보드와 동일 기준). 제외하려면 두 count 쿼리와 목록 쿼리에 `RESV_PAY_STATUS = '결제완료'` 조건을 넣으면 됨 — 사용자 결정 필요.

---

## 고객 대상 이메일 3종 추가: 회원가입 완료 / 문의 답변 완료 / 객실 예약 완료 (2026-09-22, 미커밋)

**사용자 요구:** 객실 예약하면 이메일, 문의 답장 완료하면 이메일, 가입완료 이메일.

**구현 (`EmailService`에 메서드 3개 추가, 기존 관리자용 3개는 그대로):**
- `sendSignupComplete(toEmail, nickname)` — `MemberService.signup()`에서 `memberMapper.insert` 직후 호출.
- `sendInquiryAnswered(toEmail, title, answer)` — `InquiryService.answerInquiry()`에서 답변 저장 성공 후 호출. 수신자는 문의 작성 회원(`INQUIRY.USER_MAIL`). **최초 답변 등록일 때만 발송**(답변 "수정"마다 메일이 가지 않게 저장 전 기존 답변이 비어있는지 먼저 조회).
- `sendReservationConfirmed(...)` — `PaymentController.success()`에서 토스 승인 + `markAsPaid` 성공 후 호출(`sendReservationMail`). 숙소명/객실명/플랜명/체크인·아웃/인원/금액/주문번호 포함. 수신자는 로그인 회원 이메일. 예약 정보는 세션의 `ReservationContext`에서 가져오므로 **세션이 만료돼 있으면 메일을 건너뜀**(로그만 남김, 예약·결제 자체는 이미 저장됨). 예약번호로는 DB의 RESV_NUM 대신 주문번호(orderId)를 표기.
- **실패 처리:** 세 메서드 모두 내부 `sendQuietly`로 예외를 삼키고 `log.warn`만 남김 → 메일 서버 문제가 회원가입/답변 저장/결제 완료 화면을 실패시키지 않음. (기존 관리자용 메일은 예외를 던지는 기존 동작 유지.)
- 메일 본문은 텍스트(한국어). 제목 접두어는 기존과 동일하게 `[清流庵]` 계열.

**검증:** `mvnw clean compile` BUILD SUCCESS. **실제 메일 발송/수신은 아직 확인 안 함** (회원가입 → 받은편지함, 문의 답변 등록, 결제까지 골든패스 필요).

**주의/참고:**
- 메일 발송은 요청 스레드에서 동기로 실행됨 → SMTP가 느리면 가입/답변/결제 성공 화면이 그만큼 지연됨. 체감되면 `@Async`로 전환 검토.
- 메일 계정은 `application.properties`의 Gmail 설정을 사용. **앱 비밀번호가 평문으로 커밋돼 있음(위 보안 항목 참고)** — push 전 환경변수화/재발급 필요.
- 이 개발 환경에서 수신 테스트는 실제 Gmail로 나가므로, 테스트 시 가입 이메일은 본인 수신 가능한 주소를 쓸 것.

**남은 미구현(위 항목 그대로):** 관리자 예약취소, 판매관리 온천 날짜별 예약 현황(ONSEN_RESERVATION 저장 선행 필요).

---

## 관리자 예약현황: 예약 목록 카드 클릭 시 해당 예약 상세 표시 (2026-09-22, 미커밋)

**사용자 요구:** `admin_reservation.html`의 "예약 목록" 카드를 클릭하면 그 예약의 상세가 오른쪽에 뜨게. **방식은 `admin_inquiry.html`과 동일하게.**

**방식(문의 관리와 동일):** 카드를 `<a href="/Admin/reservation_status?idx=예약번호">` 링크로 감싸고, 컨트롤러가 `idx`로 고른 예약의 상세를 모델에 담아 같은 화면을 다시 렌더링(서버 렌더링, JS/AJAX 없음). 선택된 카드는 기존 `.reservation-row-active`(금색 배경)로 강조. `idx`가 없으면 첫 번째 예약(기존 동작 유지, 사이드바 링크는 그대로 idx 없이 진입).
- `AdminController.reservationStatus()`: `@RequestParam idx` 추가. **내 예약 목록(`reservationList`) 안에서만 idx를 찾고** 없으면 첫 번째로 대체 — 다른 관리자의 예약번호를 URL에 넣어도 조회되지 않게 함(`getReservationDetail`은 관리자 검사가 없어서 목록 필터로 대신 보장).
- `admin_reservation.html`: 카드 `<div th:each>`를 `<a th:each>`로 감싸는 구조로 변경.

**검증:** `mvnw clean compile` BUILD SUCCESS. **서버 기동 후 카드 클릭 동작은 아직 확인 안 함.**

**같은 날 확인한 미구현 목록 (다음 할 일):**
- 관리자 예약취소: 상세의 "예약 취소" 버튼이 동작 없음(엔드포인트/서비스/매퍼 없음). "클릭하면 상세 구현"의 정확한 의미(확인창? 사유 입력? 토스 환불 포함 여부) 사용자 확인 필요.
- 이메일 3종 미구현: 객실 예약 완료(결제 성공 시), 문의 답변 완료(`InquiryService.answerInquiry`), 회원가입 완료(`MemberService.signup`). `EmailService`엔 관리자 신청/승인/거절 3개 메서드만 있음.
- 판매관리 "온천 관리": 카드+판매 토글만 완료. 플랜에 있는 "날짜별 예약 현황" 표는 없음 — `ONSEN_RESERVATION` 저장 자체가 미구현이라 먼저 필요.
- **보안:** `application.properties`에 Gmail 앱 비밀번호가 평문으로 있고 이미 커밋(`3382654`)에 포함됨. push 전에 환경변수화 + 비밀번호 재발급 필요.

---

## 객실현황(room_status)을 "당일 객실 관리"로 전환 — 판매가능/체크인 표시/고장 버튼 (2026-09-21, 미커밋)

**사용자 요구/결정:**
- 기존 "객실 등록·관리"(판매토글·수정·삭제 버튼)를 **당일 객실 관리**로 변경. 버튼은 `판매가능` / `체크인 표시` / `고장` 3개.
- **고장 = 판매중지(`ROOM_SALE_YN='N'`)와 동일 개념**으로 확정 (구분 불필요, DB 컬럼 추가 없음). 방이 사용불가면 계속 예약 불가하면 됨.
- **체크인 버튼**은 "오늘 체크인됐는지 빠르게 판단"하는 용도이며 **위쪽 캘린더와 연동**. 같은 DB를 읽으므로 자동 연동.
- 객실 수정(연필)/삭제 버튼은 이 화면에서 제거 (등록·수정·삭제는 `admin_info_register.html`에서). 연필은 `editRoom` JS가 이미 없어 눌러도 오류만 나던 상태였음.

**구현:**
- 체크인 상태 저장: 새 컬럼 없이 `ROOM_RESERVATION.RESV_STATUS` / `RESERVATION.RESV_STATUS`에 값 **`체크인`** 사용 (기존 `예약완료`와 전환). `RoomStatusService.STATUS_CHECKED_IN`/`STATUS_RESERVED`.
- `RoomReservationMapper(.java/.xml)`: `updateResvStatusByRoomAndDate`(RESERVATION), `updateRoomResvStatusByRoomAndDate`(ROOM_RESERVATION) 추가. 조건: 관리자·객실 일치 + 체크인일 ≤ 오늘 < 체크아웃일 + **현재 상태가 from일 때만** 변경(예약완료↔체크인만 전환, 다른 상태는 안 건드림). RESERVATION과 ROOM_RESERVATION을 같이 갱신(관리자 예약현황 화면은 RESERVATION.RESV_STATUS를 표시하므로 일치시킴).
- `RoomStatusService`: `setCheckedIn()`(@Transactional), `getTodayReservations()`(key=roomIdx), 캘린더 계산에서 체크인 여부 반영. `RoomDayStatusDto`에 `checkedIn` 필드 추가.
- `AdminController`: `POST /Admin/room_checkin`(roomIdx, checkedIn=Y/N → `/Admin/room_status`로 redirect), `roomStatus()` 모델에 `todayResvMap` 추가. 판매가능/고장은 기존 `room_toggle_sale` 재사용(Y/N).
- `room_status.html`: 캘린더 셀에 `체크인` 상태(`cell-checkedin`, `common.css`에 추가) 표시, 판매중지 문구를 `고장`으로, 범례/통계/안내문구 수정, 하단을 "당일 객실 관리" 목록으로 교체(객실별 오늘 상태 표시 + 현재 상태 버튼은 금색 강조). 체크인 버튼은 **오늘 그 객실 예약이 없거나 고장이면 비활성**, 이미 체크인이면 "체크인 취소"로 바뀜. 안 쓰는 이미지 업로드 미리보기 JS 삭제.

**검증:** `mvnw clean compile` BUILD SUCCESS. **서버 기동/화면 렌더링/버튼 동작은 아직 확인 안 함** (Thymeleaf 식은 컴파일로 검증 안 됨 — 특히 `todayResvMap[room.roomIdx]`, `th:with` 부분). 확인 순서: 오늘 걸린 예약이 있는 객실에서 체크인 표시 → 캘린더 오늘 칸이 `체크인`으로 바뀌는지 → 관리자 예약현황 상세 상태도 `체크인`인지 → 취소 시 `예약완료`로 복귀 → 고장 누르면 캘린더가 `고장`으로 바뀌고 사용자 예약 화면에서 빠지는지.

**참고/미결정:** 체크인 표시가 가능한 건 "체크인일 ≤ 오늘 < 체크아웃일"인 예약이라 2박째에도 눌러짐(문제는 없으나 필요하면 체크인일=오늘로 제한 가능). 예약 상태값 `체크인`이 사용자 마이페이지 등 다른 화면에서 어떻게 보일지는 확인 안 함(`RESV_STATUS`를 참조하는 곳: AdminReservationMapper, ReservationMapper).

---

## 관리자 화면 수정분 커밋 + 판매관리 "온천 판매 관리" 섹션 온천 데이터로 전환 (2026-09-21)

**배경:** 사용자가 직접 고친 관리자 템플릿 3개가 미커밋 상태로 남아 있었고(WORKLOG 미해결 항목 9번), 그중 `plan_sales.html`은 등록 폼을 걷어내고 3번 섹션을 "온천 판매 관리"로 바꾸다 만 상태였음.

**1) 사용자 수정분 커밋 (`dde2e51`):** 내가 만든 변경이 아니라 사용자가 직접 수정한 것을 그대로 커밋.
- `admin_reservation.html`: 예약번호를 크게, 닉네임을 작게 바꾼 표기 수정.
- `room_status.html`, `plan_sales.html`: 새 객실/플랜 등록·수정 폼 제거 (등록·수정은 `admin_info_register.html`에서만 하도록 일원화 — 화면 중복 때문).

**2) 판매관리 3번 섹션을 온천 데이터로 전환 (`220819b`):**
- 사용자 의도: 카드 UI는 플랜 카드 그대로 두고 **데이터만 플랜 → 온천(ONSEN)** 으로 교체. (사용자가 카드 형식만 복제해 둔 상태였음)
- `plan_sales.html`: `editPlan` 연필 버튼 2곳, `editPlan`/`resetPlanForm` JS, 이제 파일 입력이 없어 쓰이지 않는 이미지 업로드 미리보기 JS 전부 삭제. 3번 섹션은 `onsenList` 기반 카드로 재작성(온천명/설명/판매 토글, 가격 자리에는 이용시간 `onsenHour`). 빈 목록 안내 문구는 "정보 등록 화면에서 추가" 로 수정.
- **온천 판매 토글 신규 구현** (기존엔 없었음): `POST /Admin/onsen_toggle_sale` (`AdminController.onsenToggleSale`, 처리 후 `/Admin/plan_sales`로 redirect), `OnsenService.toggleSale`, `OnsenMapper.updateSaleYn` + `OnsenMapper.xml`의 `UPDATE ONSEN SET ONSEN_SALE_YN`. `ADMIN_IDX` 조건 포함(타 관리자 데이터 보호).
- `AdminController.planSales()`가 모델에 `onsenList`도 추가.
- **주의:** 이 토글은 `ONSEN_SALE_YN`을 바꾸므로 사용자 예약 화면의 온천 선택 노출(`OnsenMapper.findAllOnSale`)에도 그대로 반영됨.
- `mvnw clean compile` BUILD SUCCESS. **서버 기동 후 화면/토글 동작은 아직 확인 안 함.**

**참고:** 이전에 "온천 일일 판매 한도" 섹션은 목업으로 복원하기로 했었으나, 이번에 사용자가 그 자리를 "온천 판매 관리"로 직접 바꿨으므로 그 결정은 대체됨(한도 기능 자체는 여전히 미구현).

**다음 할 일 (미해결 목록은 위 섹션들의 "다음 할 일"/"아직 확인 안 됨" 참고):**
- plan_sales 온천 카드/토글 실제 동작 확인.
- 아직 push 안 함 (`origin/june47087-byte`).
- 결제 예약 저장(ORA-17004 수정 후) 골든패스 검증, 온천/식사 예약 테이블 저장 미구현 등은 그대로 남아 있음.
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
