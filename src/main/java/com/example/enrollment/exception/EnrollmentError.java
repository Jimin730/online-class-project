package com.example.enrollment.exception;

import org.springframework.http.HttpStatus;

import com.example.global.exception.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EnrollmentError implements ErrorCode {

	CANNOT_CONFIRM("수강 신청 대기 상태에서만 확정할 수 있습니다", HttpStatus.BAD_REQUEST, "E_001"),
	CANNOT_CANCEL("확정 또는 대기 상태의 수강만 취소할 수 있습니다", HttpStatus.BAD_REQUEST, "E_002");

	private final String message;
	private final HttpStatus status;
	private final String code;
}
