# 공지사항 · 1:1 문의 (유저 화면) 전달 파일

지금 깃허브(`Choiyeongsu13/RyokanMaker`)의 최상위 프로젝트(`com.mnu.ryokanmaker` 패키지, `common.css` / `header.html` / `footer.html` / 예약 페이지들이 있는 실제 활성 프로젝트) 구조와 DTO(`NoticeDto`, `InquiryDto`, `MemberDto`)를 그대로 기준으로 맞춰서 만들었습니다. 업로드해주신 목업(청류암 UI)의 색상·폰트·여백 값도 전부 `common.css`에 이미 정의돼 있길래 그 변수(`--gold`, `--ink`, `--line` 등)와 기존 클래스(`.page-title`, `.btn-gold`, `.btn-line` 등)를 그대로 재사용했습니다.

## 1. 넣는 위치

이 폴더의 `java/`와 `resources/` 안 내용을 프로젝트의 `src/main/java`, `src/main/resources`에 그대로 겹쳐서(병합) 복사하면 됩니다.

```
java/com/mnu/ryokanmaker/controller/NoticeController.java
java/com/mnu/ryokanmaker/controller/InquiryController.java
java/com/mnu/ryokanmaker/service/NoticeService.java
java/com/mnu/ryokanmaker/service/InquiryService.java
java/com/mnu/ryokanmaker/mapper/NoticeMapper.java      (인터페이스)
java/com/mnu/ryokanmaker/mapper/InquiryMapper.java     (인터페이스)

resources/mapper/NoticeMapper.xml
resources/mapper/InquiryMapper.xml
resources/templates/notice/list.html   (공지사항 목록)
resources/templates/notice/view.html   (공지사항 상세)
resources/templates/inquiry/list.html  (내 문의 목록)
resources/templates/inquiry/write.html (문의 작성)
resources/templates/inquiry/view.html  (문의 상세)

resources/static/css/common.css.append.css
  -> 이 파일 내용을 기존 static/css/common.css 맨 아래에 이어 붙여주세요 (덮어쓰기 X, 추가만).
```

## 2. 라우팅

| 화면 | URL |
|---|---|
| 공지사항 목록 | `GET /notice/list` |
| 공지사항 상세 | `GET /notice/view?idx={noticeIdx}` |
| 내 문의 목록 | `GET /inquiry/list` (로그인 필요) |
| 문의 작성 폼 | `GET /inquiry/write` (로그인 필요) |
| 문의 등록 처리 | `POST /inquiry/write` |
| 문의 상세 | `GET /inquiry/view?idx={inquiryIdx}` (본인 글만) |

## 3. 로그인 연동 (중요)

`InquiryController`는 세션 속성 `"loginMember"`에 `MemberDto`가 들어있다고 가정합니다.

```java
session.setAttribute("loginMember", member); // 로그인 성공 시 이 한 줄만 맞춰주면 바로 동작
```

지금 최상위 프로젝트에는 아직 로그인 컨트롤러가 없어서, 로그인 전에는 `/inquiry/*`가 전부 `/member/login`으로 리다이렉트됩니다(로그인 화면도 아직 없다면 그 라우트만 먼저 만들면 됩니다). 공지사항은 비로그인 상태에서도 볼 수 있게 열어뒀습니다.

## 4. adminIdx (문의 대상 료칸)

지금은 료칸이 清流庵 하나뿐인 구조라 `InquiryController`에서 기본값 1로 고정해뒀습니다 (`DEFAULT_ADMIN_IDX = 1`). ADMIN 테이블에 실제 관리자 idx가 1이 아니면 그 값으로 바꿔주세요. 나중에 료칸이 여러 개로 늘어나면 문의하기 버튼에서 `?adminIdx=` 쿼리로 넘기도록 확장하면 됩니다.

## 5. 저장소 정리 관련 참고사항

레포를 클론해서 살펴보니 최상위 `RyokanMaker/` 아래에 `RyokanMaker/RyokanMaker/...`로 프로젝트 전체가 한 번 더 중첩되어 커밋되어 있었습니다(패키지도 `com.mnu.RyokanMaker`로 대문자 버전). 그 중첩 폴더 안에 이미 만들어두셨던 1:1 문의 로직(세션 기반 로그인 체크, 목록/작성/상세 흐름)이 있길래 그 구조는 그대로 가져오고, 패키지 이름과 DTO 필드만 지금 최상위 프로젝트(`com.mnu.ryokanmaker`, `NoticeDto`/`InquiryDto`)에 맞게 다시 작성했습니다.

중첩된 `RyokanMaker/RyokanMaker/` 폴더는 실수로 이중으로 커밋된 것으로 보이니, 필요한 내용을 확인하신 후 지우고 다시 커밋하시는 걸 추천드립니다. 안에 있던 `NoticeMapper.xml`은 이전 다른 프로젝트(`tbl_notice`, `com.mnu.sample.domain.NoticeDTO` 등)에서 복사해온 것으로 보이는 내용이라 이번 스키마와 안 맞아서 참고하지 않았습니다.

## 6. 디자인

목업 중 유저 화면에서 공지사항이 단독 페이지로 나온 스크린샷은 없었고(관리자 화면의 "공지사항 입력"과 "문의 관리함"만 있었습니다), 홈페이지 하단의 공지사항 미리보기 리스트(날짜-제목 한 줄 구성)가 유일한 유저용 참고 디자인이었습니다. 그래서 공지사항 목록은 그 스타일을 그대로 페이지 전체로 확장했고, 문의 목록/작성/상세는 사이트의 나머지 페이지들(`.page-title`, 골드 포인트, 얇은 구분선)과 톤을 맞춰서 새로 짰습니다. 실제 목업이 나오면 세부 레이아웃만 조정하면 됩니다.
