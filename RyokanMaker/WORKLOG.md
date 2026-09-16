# 작업 기록

## 진행 중 (Eclipse 재시작 예정 — 여기부터 이어서)

**목표:** origin(tenyah/RyokanMaker.git)의 `Choiyeongsu13` 브랜치(팀원 통합본, 대소문자/스펠링 주의 — "choiyeonsu13" 아님)를 현재 브랜치(`june47087-byte`)로 가져오기.

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
