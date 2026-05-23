package com.example.global.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GlobalError implements ErrorCode{

	// 4xx
	INVALID_REQUEST_BODY("요청 본문이 유효하지 않습니다", HttpStatus.BAD_REQUEST, "G_001"),
	MALFORMED_JSON("잘못된 형식의 JSON입니다", HttpStatus.BAD_REQUEST, "G_002"),
	INVALID_PARAMETER_TYPE("요청 파라미터 타입이 올바르지 않습니다", HttpStatus.BAD_REQUEST, "G_003"),

	// 5xx 서버 에러
	INTERNAL_SERVER_ERROR("내부 서버 오류가 발생했습니다", HttpStatus.INTERNAL_SERVER_ERROR, "G_500");

	private final String message;
	private final HttpStatus status;
	private final String code;
}
