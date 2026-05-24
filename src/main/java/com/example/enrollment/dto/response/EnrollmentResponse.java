package com.example.enrollment.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.example.course.domain.Course;
import com.example.enrollment.domain.Enrollment;
import com.example.enrollment.domain.EnrollmentStatus;

public record EnrollmentResponse(
	Long id,
	EnrollmentStatus status,
	LocalDateTime enrolledAt,
	LocalDateTime confirmedAt,
	LocalDateTime cancelledAt,
	CourseInfo course
) {
	public record CourseInfo(
		Long id,
		String title,
		BigDecimal price,
		LocalDate startDate,
		LocalDate endDate
	) {
		static CourseInfo from(Course course) {
			return new CourseInfo(
				course.getId(),
				course.getTitle(),
				course.getPrice(),
				course.getStartDate(),
				course.getEndDate()
			);
		}
	}

	public static EnrollmentResponse of(Course course, Enrollment enrollment) {
		return new EnrollmentResponse(
			enrollment.getId(),
			enrollment.getStatus(),
			enrollment.getEnrolledAt(),
			enrollment.getConfirmedAt(),
			enrollment.getCancelledAt(),
			CourseInfo.from(course)
		);
	}
}
