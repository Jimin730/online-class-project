package com.example.global.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
		log.warn("BusinessException occurred: ErrorCode = {}, Message = {}",
			ex.getErrorCode().getCode(), ex.getMessage(), ex);

		ErrorResponse errorResponse = ErrorResponse.of(ex.getErrorCode(), ex.getMessage());

		return new ResponseEntity<>(errorResponse, ex.getErrorCode().getStatus());
	}

	@ExceptionHandler(DomainException.class)
	public ResponseEntity<ErrorResponse> handleDomainException(DomainException ex) {
		log.warn("DomainException occurred: ErrorCode = {}, Message = {}",
			ex.getErrorCode().getCode(), ex.getMessage(), ex);

		ErrorResponse errorResponse = ErrorResponse.of(ex.getErrorCode(), ex.getMessage());

		return new ResponseEntity<>(errorResponse, ex.getErrorCode().getStatus());
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
		log.error("Unexpected error occurred: {}", ex.getMessage(), ex);

		ErrorResponse errorResponse = ErrorResponse.of("UNKNOWN_ERROR", "An unexpected error occurred");

		return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
	}
}