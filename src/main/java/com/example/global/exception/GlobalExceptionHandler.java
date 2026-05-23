package com.example.global.exception;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
		ErrorResponse errorResponse = ErrorResponse.of(ex.getErrorCode());
		log.warn("[BusinessException] code={}, message={}",
			errorResponse.errorCode(), errorResponse.message());

		return new ResponseEntity<>(errorResponse, ex.getErrorCode().getStatus());
	}

	@ExceptionHandler(DomainException.class)
	public ResponseEntity<ErrorResponse> handleDomainException(DomainException ex) {
		ErrorResponse errorResponse = ErrorResponse.of(ex.getErrorCode());
		log.warn("[DomainException] code={}, message={}",
			errorResponse.errorCode(), errorResponse.message());

		return new ResponseEntity<>(errorResponse, ex.getErrorCode().getStatus());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
		List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
		ErrorResponse errorResponse = ErrorResponse.of(GlobalError.INVALID_REQUEST_BODY, fieldErrors);
		log.warn("[ValidationException] code={}, details={}",
			errorResponse.errorCode(), errorResponse.details());

		return new ResponseEntity<>(errorResponse, GlobalError.INVALID_REQUEST_BODY.getStatus());
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
		ErrorResponse errorResponse = ErrorResponse.of(GlobalError.MALFORMED_JSON);
		log.warn("[MalformedJsonException] code={}, message={}",
			errorResponse.errorCode(), ex.getMessage());

		return new ResponseEntity<>(errorResponse, GlobalError.MALFORMED_JSON.getStatus());
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
		ErrorResponse errorResponse = ErrorResponse.of(GlobalError.INVALID_PARAMETER_TYPE);
		log.warn("[ParameterTypeMismatchException] code={}, parameter={}, value={}",
			errorResponse.errorCode(), ex.getName(), ex.getValue());

		return new ResponseEntity<>(errorResponse, GlobalError.INVALID_PARAMETER_TYPE.getStatus());
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleException(Exception ex) {
		ErrorResponse errorResponse = ErrorResponse.of(GlobalError.INTERNAL_SERVER_ERROR);
		log.error("[UnexpectedException] code={}, message={}",
			errorResponse.errorCode(), ex.getMessage(), ex);

		return new ResponseEntity<>(errorResponse, GlobalError.INTERNAL_SERVER_ERROR.getStatus());
	}
}