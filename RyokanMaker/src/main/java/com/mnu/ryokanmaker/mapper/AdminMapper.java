package com.mnu.ryokanmaker.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.mnu.ryokanmaker.domain.AdminDTO;

@Mapper
public interface AdminMapper {
	public AdminDTO adminLogin();
}
