package com.example.course.exception;

import org.springframework.http.HttpStatus;

import com.example.global.exception.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CourseError implements ErrorCode {

	ALREADY_OPENED("이미 모집중인 강의입니다", HttpStatus.BAD_REQUEST, "C_001"),
	CANNOT_CLOSED("모집중인 강의만 마감할 수 있습니다 ", HttpStatus.BAD_REQUEST, "C_002");

	private final String message;
	private final HttpStatus status;
	private final String code;
}
