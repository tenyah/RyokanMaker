package com.mnu.ryokanmaker.mappers;

import com.mnu.ryokanmaker.domain.CourseDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CourseMapper {
    List<CourseDTO> findAll();
}