package com.mnu.ryokanmaker.mapper;

import com.mnu.ryokanmaker.dto.AdminDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AdminMapper {

    /** 아이디 중복 확인 / 로그인 조회 겸용 */
    AdminDto selectByAdminId(String adminId);

    AdminDto selectByAdminIdx(Integer adminIdx);

    /** 관리자 계정 등록 (계정 신청 승인 시 사용) */
    int insert(AdminDto adminDto);

    /** 여관 기본정보 수정 (인덱스 화면). 1 관리자 = 1 여관이라 등록/삭제 없이 수정만 있음. */
    int updateRyokanInfo(AdminDto adminDto);

    /** 교통안내(RYOKAN_ACCESS) 단독 수정 */
    int updateRyokanAccess(AdminDto adminDto);

    /** /access, /Admin/access_edit 전용 단순 업데이트 (updateRyokanAccess와 동일 컬럼) */
    int updateAccess(@Param("adminIdx") Integer adminIdx, @Param("ryokanAccess") String ryokanAccess);

    /** 비밀번호 변경. 변경 성공 시 PW_RESET_YN도 'Y'로 같이 갱신된다(이후 로그인은 해시 비교). */
    int updatePassword(@Param("adminIdx") Integer adminIdx, @Param("adminPassword") String adminPassword);
}
