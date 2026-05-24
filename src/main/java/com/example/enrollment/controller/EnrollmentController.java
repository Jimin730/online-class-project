package com.example.enrollment.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.enrollment.dto.request.EnrollmentCreateRequest;
import com.example.enrollment.dto.response.EnrollmentCreateResponse;
import com.example.enrollment.service.EnrollmentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

	private final EnrollmentService enrollmentService;

	@PostMapping
	public ResponseEntity<EnrollmentCreateResponse> enroll(
		@RequestHeader("X-User-Id") Long studentId,
		@Valid @RequestBody EnrollmentCreateRequest request
	) {
		EnrollmentCreateResponse response = enrollmentService.createEnrollment(studentId, request.courseId());
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}
}
