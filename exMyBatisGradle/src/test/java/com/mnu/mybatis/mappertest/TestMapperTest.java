package com.mnu.mybatis.mappertest;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.mnu.mybatis.mapper.TestMapper;

@SpringBootTest
public class TestMapperTest {
	//로그 출력용
	private static final Logger log =
			LoggerFactory.getLogger(TestMapperTest.class);
	
	@Autowired
	private TestMapper mapper;

	@Test
	public void testGetTime() {
		log.info("클래스 : " + mapper.getClass().getName());
		log.info("오늘 날자는 : " + mapper.getTime());
	}
	
/*	
	@Test
	public void testGetTime2() {
		log.info("오늘 날자는 2 : " + mapper.getTime2());
	}
*/
/*	
	@Test
	public void testEmpCount() {
		log.info("사원수2  : " + mapper.empCount());
	}
*/
	@Test
	public void boardCountTest() {
		int row = mapper.boardCount();
		log.info("게시글 수 : " + row);
	}
}
