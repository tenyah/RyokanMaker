package com.mnu.mybatis.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface TestMapper {
	@Select("select sysdate from dual")
	public String getTime();
	
	public String getTime2();
	
	//emp 테이블의 사원수 카운트
	public int empCount();
	
	public int boardCount();
}
