package com.example.enrollment.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.enrollment.dto.request.EnrollmentCreateRequest;
import com.example.enrollment.dto.response.EnrollmentCreateResponse;
import com.example.enrollment.dto.response.EnrollmentResponse;
import com.example.enrollment.service.EnrollmentService;
import com.example.global.dto.SliceResponse;

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

	@PostMapping("/{enrollmentId}/confirm")
	public ResponseEntity<Void> confirm(
		@RequestHeader("X-User-Id") Long studentId,
		@PathVariable Long enrollmentId
	) {
		enrollmentService.confirmEnrollment(studentId, enrollmentId);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/{enrollmentId}/cancel")
	public ResponseEntity<Void> cancel(
		@RequestHeader("X-User-Id") Long studentId,
		@PathVariable Long enrollmentId
	) {
		enrollmentService.cancelEnrollment(studentId, enrollmentId);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/my")
	public ResponseEntity<SliceResponse<EnrollmentResponse>> getMyEnrollments(
		@RequestHeader("X-User-Id") Long studentId,
		@PageableDefault(size = 10, sort = "enrolledAt", direction = Sort.Direction.DESC) Pageable pageable
	) {
		Slice<EnrollmentResponse> slice = enrollmentService.getMyEnrollments(studentId, pageable);
		return ResponseEntity.ok(SliceResponse.from(slice));
	}
}
