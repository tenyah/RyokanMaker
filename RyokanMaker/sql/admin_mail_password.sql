-- 료칸 관리자 메일(ADMIN.ADMIN_MAIL) 주소로 손님에게 메일(비밀번호 찾기 등)을 직접 보내기 위한
-- 메일 앱 비밀번호 보관용 컬럼. 평문이 아니라 AES-GCM으로 암호화한 값(Base64)이 저장된다.
-- 암호화 키는 환경변수 MAIL_SECRET_KEY 로 서버에만 두고 DB/코드에는 두지 않는다.

ALTER TABLE ADMIN ADD (MAIL_APP_PASSWORD VARCHAR2(500));
