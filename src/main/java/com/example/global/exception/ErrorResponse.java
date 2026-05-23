package com.example.global.exception;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.validation.FieldError;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
	String errorCode,
	String message,
	Object details
) {

	public static ErrorResponse of(ErrorCode errorCode) {
		return new ErrorResponse(errorCode.getCode(), errorCode.getMessage(), null);
	}

	public static ErrorResponse of(ErrorCode errorCode, Object details) {
		return new ErrorResponse(errorCode.getCode(), errorCode.getMessage(), details);
	}

	public static ErrorResponse of(ErrorCode errorCode, List<FieldError> fieldErrors) {
		Map<String, String> errorDetails = new HashMap<>();
		fieldErrors.forEach(error ->
			errorDetails.put(error.getField(), error.getDefaultMessage())
		);

		return new ErrorResponse(errorCode.getCode(), errorCode.getMessage(), errorDetails);
	}
}