package com.mnu.ryokanmaker.mappers;

import com.mnu.ryokanmaker.domain.OnsenDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface OnsenMapper {
    List<OnsenDTO> findAll();
}
