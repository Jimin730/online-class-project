package com.example.course.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.course.dto.request.CourseCreateRequest;
import com.example.course.dto.response.CourseResponse;
import com.example.course.service.CourseService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

	private final CourseService courseService;

	@PostMapping
	public ResponseEntity<CourseResponse> create(
		@RequestHeader("X-User-Id") Long teacherId,
		@Valid @RequestBody CourseCreateRequest request
	) {
		CourseResponse response = courseService.createCourse(teacherId, request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping("/{courseId}")
	public ResponseEntity<CourseResponse> getCourse(@PathVariable Long courseId) {
		CourseResponse response = courseService.getCourse(courseId);
		return ResponseEntity.ok(response);
	}
}