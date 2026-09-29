-- 회원(MEMBER)을 료칸(ADMIN)별로 완전히 분리한다.
-- 지금까지는 USER_MAIL 하나가 전역 PK라서, 같은 이메일이 료칸 A/B에 각각 가입할 수 없었다.
-- 이제 MEMBER의 PK를 (ADMIN_IDX, USER_MAIL) 복합키로 바꿔서, 같은 이메일도 료칸마다 독립된 회원이 되게 한다.
--
-- 모든 단계가 "이미 끝났으면 건너뛴다" 식으로 짜여 있어서, 처음부터 실행하든
-- 중간에 실패한 뒤 다시 실행하든 안전하다(2026-09-29 EC2 DB에서 한 번 중간에 막힌 뒤 이 버전으로 교체됨:
-- 옛 PK가 자식 테이블 FK에 걸려서 못 지워지는 문제 -> 자식 FK를 먼저 지우도록 순서를 바꿈).

-- 0) MEMBER에 ADMIN_IDX 추가 -> 기존 회원은 전부 관리자 1번(清流庵) 소속으로 백필 -> NOT NULL
DECLARE
    v_cnt NUMBER;
BEGIN
    SELECT COUNT(*) INTO v_cnt FROM user_tab_columns WHERE table_name = 'MEMBER' AND column_name = 'ADMIN_IDX';
    IF v_cnt = 0 THEN
        EXECUTE IMMEDIATE 'ALTER TABLE MEMBER ADD ADMIN_IDX NUMBER';
    END IF;
END;
/

UPDATE MEMBER SET ADMIN_IDX = 1 WHERE ADMIN_IDX IS NULL;
COMMIT;

DECLARE
    v_nullable VARCHAR2(1);
BEGIN
    SELECT nullable INTO v_nullable FROM user_tab_columns WHERE table_name = 'MEMBER' AND column_name = 'ADMIN_IDX';
    IF v_nullable = 'Y' THEN
        EXECUTE IMMEDIATE 'ALTER TABLE MEMBER MODIFY ADMIN_IDX NOT NULL';
    END IF;
END;
/

-- 1) 자식 테이블 5개에 남아있는 옛 단일컬럼(USER_MAIL) FK를 전부 제거
--    (MEMBER의 PK를 바꾸려면, 그 PK를 참조하는 자식 FK를 먼저 없애야 한다)
DECLARE
    TYPE t_tables IS TABLE OF VARCHAR2(30);
    v_tables t_tables := t_tables('RESERVATION', 'INQUIRY', 'ROOM_RESERVATION', 'RESTAURANT_RESERVATION', 'ONSEN_RESERVATION');
    v_fk_name VARCHAR2(128);
BEGIN
    FOR i IN 1 .. v_tables.COUNT LOOP
        BEGIN
            SELECT uc.constraint_name INTO v_fk_name
            FROM user_constraints uc
            JOIN user_cons_columns ucc ON ucc.constraint_name = uc.constraint_name
            WHERE uc.table_name = v_tables(i)
              AND uc.constraint_type = 'R'
              AND ucc.column_name = 'USER_MAIL'
              AND (SELECT COUNT(*) FROM user_cons_columns c2 WHERE c2.constraint_name = uc.constraint_name) = 1;

            EXECUTE IMMEDIATE 'ALTER TABLE ' || v_tables(i) || ' DROP CONSTRAINT ' || v_fk_name;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                NULL; -- 이미 지워졌거나(예: RESERVATION) 애초에 없으면 건너뜀
        END;
    END LOOP;
END;
/

-- 2) MEMBER의 옛 단일 PK(USER_MAIL)를 복합 PK(ADMIN_IDX, USER_MAIL)로 교체
--    (이미 복합키로 바뀌어 있으면 건너뜀)
DECLARE
    v_pk_name VARCHAR2(128);
    v_pk_cols NUMBER;
BEGIN
    SELECT constraint_name INTO v_pk_name
    FROM user_constraints
    WHERE table_name = 'MEMBER' AND constraint_type = 'P';

    SELECT COUNT(*) INTO v_pk_cols
    FROM user_cons_columns
    WHERE constraint_name = v_pk_name;

    IF v_pk_cols = 1 THEN
        EXECUTE IMMEDIATE 'ALTER TABLE MEMBER DROP CONSTRAINT ' || v_pk_name;
        EXECUTE IMMEDIATE 'ALTER TABLE MEMBER ADD CONSTRAINT PK_MEMBER PRIMARY KEY (ADMIN_IDX, USER_MAIL)';
    END IF;
END;
/

-- 3) MEMBER.ADMIN_IDX -> ADMIN.ADMIN_IDX FK (이미 있으면 건너뜀)
DECLARE
    v_cnt NUMBER;
BEGIN
    SELECT COUNT(*) INTO v_cnt
    FROM user_constraints
    WHERE table_name = 'MEMBER' AND constraint_name = 'FK_ADMIN_TO_MEMBER';

    IF v_cnt = 0 THEN
        EXECUTE IMMEDIATE 'ALTER TABLE MEMBER ADD CONSTRAINT FK_ADMIN_TO_MEMBER FOREIGN KEY (ADMIN_IDX) REFERENCES ADMIN (ADMIN_IDX)';
    END IF;
END;
/

-- 4) 자식 테이블 5개에 (ADMIN_IDX, USER_MAIL) 복합 FK를 새로 생성
--    (RESERVATION/INQUIRY/ROOM_RESERVATION/RESTAURANT_RESERVATION/ONSEN_RESERVATION은
--     이미 ADMIN_IDX, USER_MAIL 컬럼을 둘 다 갖고 있어서 컬럼 추가는 필요 없음, FK만 다시 생성)
DECLARE
    TYPE t_tables IS TABLE OF VARCHAR2(30);
    v_tables t_tables := t_tables('RESERVATION', 'INQUIRY', 'ROOM_RESERVATION', 'RESTAURANT_RESERVATION', 'ONSEN_RESERVATION');
    v_cnt NUMBER;
BEGIN
    FOR i IN 1 .. v_tables.COUNT LOOP
        SELECT COUNT(*) INTO v_cnt
        FROM user_constraints
        WHERE table_name = v_tables(i) AND constraint_name = 'FK_MEMBER_TO_' || v_tables(i);

        IF v_cnt = 0 THEN
            EXECUTE IMMEDIATE 'ALTER TABLE ' || v_tables(i) ||
                ' ADD CONSTRAINT FK_MEMBER_TO_' || v_tables(i) ||
                ' FOREIGN KEY (ADMIN_IDX, USER_MAIL) REFERENCES MEMBER (ADMIN_IDX, USER_MAIL)';
        END IF;
    END LOOP;
END;
/

-- 확인용 : MEMBER는 PK가 (ADMIN_IDX, USER_MAIL) 2줄로, 나머지 5개는 FK_MEMBER_TO_*가 전부 ENABLED로 나와야 정상
SELECT constraint_name, table_name, constraint_type, status
FROM user_constraints
WHERE table_name IN ('MEMBER', 'RESERVATION', 'INQUIRY', 'ROOM_RESERVATION', 'RESTAURANT_RESERVATION', 'ONSEN_RESERVATION')
  AND constraint_type IN ('P', 'R')
ORDER BY table_name;

SELECT constraint_name, column_name, position
FROM user_cons_columns
WHERE constraint_name = 'PK_MEMBER'
ORDER BY position;
