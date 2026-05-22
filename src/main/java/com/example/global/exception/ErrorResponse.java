package com.example.global.exception;

public record ErrorResponse(
	String errorCode, String message
) {

	public static ErrorResponse of(ErrorCode errorCode, String message) {
		return new ErrorResponse(errorCode.getCode(), message);
	}

	public static ErrorResponse of(String code, String message) {
		return new ErrorResponse(code, message);
	}
}