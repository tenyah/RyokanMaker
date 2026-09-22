package com.mnu.ryokanmaker.mapper;

import com.mnu.ryokanmaker.domain.AdminRequestDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AdminRequestMapper {

    /** 관리자 계정 신청 등록 (상태는 XML에서 '대기'로 고정) */
    int insert(AdminRequestDto requestDto);

    /** 대기 중인 신청 목록 (최신순) */
    List<AdminRequestDto> selectPending();

    /** 신청 단건 조회 */
    AdminRequestDto selectByIdx(int requestIdx);

    /** 승인 처리 : 상태 '승인' + 처리일시 + 발급된 AdminIdx 기록 */
    int approve(@Param("requestIdx") int requestIdx, @Param("resultAdminIdx") int resultAdminIdx);

    /** 반려 처리 : 상태 '반려' + 처리일시 */
    int reject(int requestIdx);
}
