package com.mnu.ryokanmaker.service;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.mnu.ryokanmaker.dto.RoomDto;
import com.mnu.ryokanmaker.mapper.RoomMapper;
import com.mnu.ryokanmaker.util.ImageJsonUtil;

@Service
public class RoomService {

	private static final int MAX_IMAGES = 10;

	@Autowired
	private RoomMapper roomMapper;

	public List<RoomDto> getRoomList(Integer adminIdx) {
		return roomMapper.selectRoomsByAdmin(adminIdx);
	}

	/**
	 * roomIdx가 없으면(0 또는 null) 신규 등록, 있으면 수정으로 처리 (upsert)
	 * roomImageFiles : 최대 10장, 파일로 저장 후 경로 배열을 JSON 문자열로 ROOM_IMAGE(CLOB)에 저장.
	 * 수정 시 새 이미지를 올리지 않으면 기존 이미지를 그대로 유지함.
	 */
	public void saveRoom(RoomDto roomDto, List<MultipartFile> roomImageFiles) throws IOException {

		String imageJson = ImageJsonUtil.toJson(roomImageFiles, MAX_IMAGES, "room");
		if (imageJson != null) {
			roomDto.setRoomImage(imageJson);
		}

		boolean isUpdate = roomDto.getRoomIdx() != null && roomDto.getRoomIdx() > 0;
		if (isUpdate) {
			roomMapper.updateRoom(roomDto);
		} else {
			roomMapper.insertRoom(roomDto);
		}
	}

	public void deleteRoom(Integer roomIdx, Integer adminIdx) {
		roomMapper.deleteRoom(roomIdx, adminIdx);
	}
}
