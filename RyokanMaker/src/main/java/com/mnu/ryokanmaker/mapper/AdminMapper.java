package com.mnu.ryokanmaker.mapper;

import com.mnu.ryokanmaker.dto.AdminDto;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AdminMapper {

    /** 아이디 중복 확인 / 로그인 조회 겸용 */
    AdminDto selectByAdminId(String adminId);

    /** 관리자 계정 등록 (계정 신청 승인 시 사용) */
    int insert(AdminDto adminDto);
}
