package com.example.user.exception;

import org.springframework.http.HttpStatus;

import com.example.global.exception.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserError implements ErrorCode {

	USER_NOT_FOUND("사용자를 찾을 수 없습니다", HttpStatus.NOT_FOUND, "U_001"),
	FORBIDDEN_TEACHER_ONLY("강사 권한이 필요합니다", HttpStatus.FORBIDDEN, "U_002");

	private final String message;
	private final HttpStatus status;
	private final String code;
}
