package com.mnu.ryokanmaker.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.mnu.ryokanmaker.domain.AdminDTO;

@Mapper
public interface AdminMapper {
	public AdminDTO adminLogin(AdminDTO adminDTO);
	public int updateRyokanInfo(AdminDTO adminDTO);
	public int updateRyokanAccess(AdminDTO adminDTO);
	public int updatePassword(AdminDTO adminDTO);
}
