package com.mnu.ryokanmaker.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.mnu.ryokanmaker.domain.AdminDTO;

@Mapper
public interface AdminMapper {
	public AdminDTO findByAdminId(@Param("admin_id") String adminId);
	public int updateRyokanInfo(AdminDTO adminDTO);
	public int updateRyokanAccess(AdminDTO adminDTO);
	public int updatePassword(AdminDTO adminDTO);
}
