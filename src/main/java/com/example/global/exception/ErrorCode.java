package com.example.global.exception;

import org.springframework.http.HttpStatus;

public interface ErrorCode {

	String getMessage();
	HttpStatus getStatus();
	String getCode();
}
