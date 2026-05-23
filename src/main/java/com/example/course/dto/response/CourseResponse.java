package com.example.course.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.example.course.domain.Course;
import com.example.course.domain.CourseStatus;

public record CourseResponse(
	Long id,
	Long teacherId,
	String title,
	String description,
	BigDecimal price,
	int capacity,
	int enrolledCount,
	LocalDate startDate,
	LocalDate endDate,
	CourseStatus status
) {

	public static CourseResponse from(Course course) {
		return new CourseResponse(
			course.getId(),
			course.getTeacherId(),
			course.getTitle(),
			course.getDescription(),
			course.getPrice(),
			course.getCapacity(),
			course.getEnrolledCount(),
			course.getStartDate(),
			course.getEndDate(),
			course.getStatus()
		);
	}
}
