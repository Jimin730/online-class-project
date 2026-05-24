package com.example.course.exception;

import org.springframework.http.HttpStatus;

import com.example.global.exception.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CourseError implements ErrorCode {

	CANNOT_OPEN("초안 상태의 강의만 모집을 시작할 수 있습니다", HttpStatus.BAD_REQUEST, "C_001"),
	CANNOT_CLOSED("모집중인 강의만 마감할 수 있습니다 ", HttpStatus.BAD_REQUEST, "C_002"),
	INVALID_PERIOD("종료일은 시작일 이후여야 합니다", HttpStatus.BAD_REQUEST, "C_003"),
	NOT_FOUND_COURSE("강의를 찾을 수 없습니다", HttpStatus.NOT_FOUND, "C_004"),
	NOT_COURSE_OWNER("강의 소유자만 변경이 가능합니다", HttpStatus.FORBIDDEN, "C_005"),
	NOT_OPEN("모집중인 강의가 아닙니다", HttpStatus.BAD_REQUEST, "C_006"),
	CAPACITY_EXCEEDED("정원이 초과되었습니다", HttpStatus.BAD_REQUEST, "C_007");

	private final String message;
	private final HttpStatus status;
	private final String code;
}
