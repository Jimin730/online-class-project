package com.example.course.controller;

import java.net.URI;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.course.domain.CourseStatus;
import com.example.course.dto.request.CourseCreateRequest;
import com.example.course.dto.response.CourseCreateResponse;
import com.example.course.dto.response.CourseListResponse;
import com.example.course.dto.response.CourseResponse;
import com.example.course.service.CourseService;
import com.example.global.dto.SliceResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

	private final CourseService courseService;

	@PostMapping
	public ResponseEntity<CourseCreateResponse> create(
		@RequestHeader("X-User-Id") Long teacherId,
		@Valid @RequestBody CourseCreateRequest request
	) {
		Long courseId = courseService.createCourse(teacherId, request);
		CourseCreateResponse response = new CourseCreateResponse(courseId);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping("/{courseId}")
	public ResponseEntity<CourseResponse> getCourse(@PathVariable Long courseId) {
		CourseResponse response = courseService.getCourse(courseId);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/{courseId}/open")
	public ResponseEntity<Void> open(
		@RequestHeader("X-User-Id") Long teacherId,
		@PathVariable Long courseId
	) {
		courseService.openCourse(teacherId, courseId);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/{courseId}/close")
	public ResponseEntity<Void> close(
		@RequestHeader("X-User-Id") Long teacherId,
		@PathVariable Long courseId
	) {
		courseService.closeCourse(teacherId, courseId);
		return ResponseEntity.noContent().build();
	}

	@GetMapping
	public ResponseEntity<SliceResponse<CourseListResponse>> getCourses(
		@RequestParam(required = false) CourseStatus status,
		@PageableDefault(size = 20) Pageable pageable
	) {
		Slice<CourseListResponse> slice = courseService.getCourses(status, pageable);
		return ResponseEntity.ok(SliceResponse.from(slice));
	}
}