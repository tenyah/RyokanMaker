# 🏯 Ryokan Maker

> **료칸 전용 웹사이트 구축·운영 솔루션**
>
> 웹사이트가 없는 료칸도 관리자 페이지에서 쉽게 자기 사이트를 만들고, 객실·온천·가이세키 예약과 결제, 매출 관리까지 한곳에서 처리할 수 있는 플랫폼입니다.

<br/>

## 📘 목차

1. [프로젝트 소개](#-프로젝트-소개)
2. [팀원 소개 및 업무 분담](#-팀원-소개-및-업무-분담)
3. [기술 스택](#-기술-스택)
4. [주요 기능](#-주요-기능)
5. [ERD](#-erd)
6. [이용 시나리오](#-이용-시나리오)
7. [배포](#-배포)

<br/>

## 🏝 프로젝트 소개

### 개발 기간
2026.09.14 ~ 2026.09.23 (발표 2026.09.28)

### 기획 배경

| 문제 | 내용 |
|---|---|
| **웹사이트 제작의 어려움** | 자체 웹사이트가 없는 료칸이 많고, 사이트를 만들고 고치려면 개발자가 필요함 |
| **불편한 예약 동선** | 객실 외 부대시설(온천·식사)은 현지에서 따로 예약해야 하고, 외국인 관광객은 언어 장벽이 있음 |
| **자원 낭비** | 당일 예약·취소로 비싼 가이세키 요리 식재료가 폐기됨 |

### 해결 방법

- **노코드 웹사이트 빌더**: 소스 코드 수정 없이 관리자 페이지에서 텍스트·이미지·요금·플랜·공지사항을 바로 수정하고 공개 페이지에 반영
- **결제 연동형 예약 엔진**: 객실·전세 온천·가이세키를 한 번에 예약하고 사전 결제로 예약을 확정
- **경영 관리 대시보드**: 예약·매출 현황을 그래프로 보고 Excel로 내려받기
- **다국어 지원**: 한국어·영어·일본어 전환과 자동 번역, 원화·달러 환율 환산

<br/>

## 👫 팀원 소개 및 업무 분담

| 이름 | 역할 | 담당 업무 |
| :---: | :---: | :--- |
| **김지안** | PM / 풀스택 | 관리자 화면 UI·기능 설계, 매출 대시보드 구현 |
| **김형준** | 백엔드 / 결제 | 결제 PG(토스페이먼츠) 연동, 예약 완료 메일 자동 발송(Gmail SMTP), 운영 현황(객실·판매·예약) 구현 |
| **은예성** | 풀스택 / 예약 | 사용자 페이지 UI 디자인, 예약 엔진(필터링·가격 계산) 구현, 다국어 환율 연동 |
| **최영수** | 인프라 / 백엔드 | DB·서버 인프라 관리(AWS), 다국어 번역 API 연동 |

<br/>

## 🛠 기술 스택

### Backend
<p>
  <img src="https://img.shields.io/badge/Java%2021-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white">
  <img src="https://img.shields.io/badge/Spring%20Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white">
  <img src="https://img.shields.io/badge/Spring%20MVC-6DB33F?style=for-the-badge&logo=spring&logoColor=white">
  <img src="https://img.shields.io/badge/MyBatis-000000?style=for-the-badge&logoColor=white">
  <img src="https://img.shields.io/badge/Lombok-BC4521?style=for-the-badge&logoColor=white">
</p>

### Frontend
<p>
  <img src="https://img.shields.io/badge/Thymeleaf-005F0F?style=for-the-badge&logo=thymeleaf&logoColor=white">
  <img src="https://img.shields.io/badge/HTML5-E34F26?style=for-the-badge&logo=html5&logoColor=white">
  <img src="https://img.shields.io/badge/CSS3-1572B6?style=for-the-badge&logo=css3&logoColor=white">
  <img src="https://img.shields.io/badge/JavaScript-F7DF1E?style=for-the-badge&logo=javascript&logoColor=black">
  <img src="https://img.shields.io/badge/Chart.js-FF6384?style=for-the-badge&logo=chartdotjs&logoColor=white">
</p>

### Database & Infra
<p>
  <img src="https://img.shields.io/badge/Oracle-F80000?style=for-the-badge&logo=oracle&logoColor=white">
  <img src="https://img.shields.io/badge/AWS-232F3E?style=for-the-badge&logo=amazonaws&logoColor=white">
</p>

### Tools
<p>
  <img src="https://img.shields.io/badge/Git-F05032?style=for-the-badge&logo=git&logoColor=white">
  <img src="https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=github&logoColor=white">
  <img src="https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white">
  <img src="https://img.shields.io/badge/Apache%20POI-D22128?style=for-the-badge&logo=apache&logoColor=white">
</p>

### 외부 API

| API | 용도 |
|---|---|
| TossPayments | 온라인 결제 | https://docs.tosspayments.com/blog/how-to-test-toss-payments
| Google Gemini | 자동 번역 | https://ai.google.dev/gemini-api/docs?hl=ko
| Frankfurter API | 환율(매일 갱신) | https://api.frankfurter.dev/v1/latest?base=JPY&symbols=KRW,USD

<br/>

## 💡 주요 기능

### 1. 노코드 웹사이트 빌더
<img width="1518" height="686" alt="Image" src="https://github.com/user-attachments/assets/7b0bca91-29e9-4c1f-97c7-01f33e1984eb" />

- 소스 코드 수정 없이 관리자 페이지에서 텍스트·이미지·요금 정보를 수정
- 계절 한정 플랜과 공지사항을 개발자 없이 바로 사이트에 반영

### 2. 결제 연동형 예약 엔진
<img width="1518" height="686" alt="Image" src="https://github.com/user-attachments/assets/66edb9e6-e93c-408a-9874-656a2e33a3b8" />

- 객실·온천·가이세키 요리를 한 번에 예약
- 날짜·인원 조건으로 예약 가능한 객실을 필터링하고 요금 계산
- **토스페이먼츠 연동으로 사전 결제 후 예약 자동 확정**
- **결제 금액 검증**: 【클라이언트에서 넘어온 금액과 서버에서 계산한 금액을 비교하는 등, 실제로 구현한 방식】
- **예약 완료 메일**: 예약이 확정되면 Gmail SMTP로 예약 내용을 담은 메일을 자동 발송

### 3. 경영 관리 대시보드
<img width="1518" height="686" alt="Image" src="https://github.com/user-attachments/assets/3fedd10b-bc4f-4618-9bf3-ebe7fc9356ae" />

- 예약 현황·매출·시설 이용률을 Chart.js 그래프로 표시
- 날짜별 예약 데이터 조회
- **운영 현황(객실·판매·예약) 관리**: 【실제로 만든 기능 — 목록, 검색, 상태 변경 등】
- Apache POI로 매출 데이터 Excel 출력

### 4. 다국어 지원
<img width="837" height="617" alt="Image" src="https://github.com/user-attachments/assets/7f25f607-9eee-4c65-a7d3-1bd39dc7749e" />

- 한국어·영어·일본어 전환 시 선택한 언어로 페이지 내용 표시
- Google Gemini API로 사이트 텍스트 자동 번역
- Frankfurter API로 요금을 원화·달러로 환산(환율 매일 갱신)

<br/>

## ✍🏻 시스템 아키텍쳐

<img width="2376" height="1559" alt="Image" src="https://github.com/user-attachments/assets/51858a19-aa53-40dd-a22b-687d5d3fa336" />

<br/>

## ✍🏻 ERD

<img width="1880" height="968" alt="Image" src="https://github.com/user-attachments/assets/dbbe4f53-a9ba-4774-8740-a6ebc551d12d" />

<br/>

## 🗺 이용 시나리오

| 단계 | 사용자 | 흐름 |
|---|---|---|
| **Phase 1** 시설 정보 등록 | 관리자 | ① 관리자 로그인 → ② 객실·전세 온천·가이세키·시설·플랜 등록 → 공개 페이지에 즉시 반영 |
| **Phase 2** 열람·예약·문의 | 회원 | ③ 공개 페이지 열람 → ④ 로그인/회원가입 → ⑤ 플랜 선택 → 결제 → ⑥ 마이페이지에서 예약 내역 확인·문의 |
| **Phase 3** 확인·대응·매출 | 관리자 | ⑦ 신규 예약 확인 → ⑧ 문의 답변 등록 → ⑨ 일별 매출 집계·Excel 출력 |

<br/>

## 🚀 배포

### 1. 필요 환경
- JDK 21
- Maven
- Oracle Database

### 2. 프로젝트 클론
```bash
git clone 【clone https://github.com/tenyah/ryokan-maker.git】
cd 【ryokan-maker】
```

### 3. 환경변수 설정
API 키는 저장소에 올리지 않고 환경변수로 관리합니다.

| 환경변수 | 설명 |
|---|---|
| `DB_URL` / `DB_USERNAME` / `DB_PASSWORD` | Oracle 접속 정보 |
| `TOSS_CLIENT_KEY` / `TOSS_SECRET_KEY` | 토스페이먼츠 키 |
| `GEMINI_API_KEY` | Google Gemini API 키 |
| `MAIL_USERNAME` / `MAIL_APP_PASSWORD` | Gmail 계정과 앱 비밀번호 |

```properties
# src/main/resources/application.properties
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=${MAIL_USERNAME}
spring.mail.password=${MAIL_APP_PASSWORD}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

> Frankfurter API는 키 없이 사용할 수 있습니다.

### 4. 실행
```bash
mvn spring-boot:run
```
실행 후 `http://localhost:8080` 로 접속합니다.

### 5. 실제 배포 링크
aws로 웹상에 배포한 홈페이지 링크입니다.
http://3.36.211.118:8080/r/test
