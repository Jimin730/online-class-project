package com.example.course.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.example.course.domain.Course;
import com.example.course.domain.CourseStatus;

public record CourseListResponse(
	Long id,
	Long teacherId,
	String title,
	BigDecimal price,
	int capacity,
	int enrolledCount,
	LocalDate startDate,
	LocalDate endDate,
	CourseStatus status
) {
	public static CourseListResponse from(Course course) {
		return new CourseListResponse(
			course.getId(),
			course.getTeacherId(),
			course.getTitle(),
			course.getPrice(),
			course.getCapacity(),
			course.getEnrolledCount(),
			course.getStartDate(),
			course.getEndDate(),
			course.getStatus()
		);
	}
}
