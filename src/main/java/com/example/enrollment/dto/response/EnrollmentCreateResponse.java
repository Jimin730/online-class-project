package com.example.enrollment.dto.response;

import java.time.LocalDateTime;

import com.example.enrollment.domain.Enrollment;
import com.example.enrollment.domain.EnrollmentStatus;

public record EnrollmentCreateResponse(
	Long id,
	Long courseId,
	EnrollmentStatus status,
	LocalDateTime enrolledAt
) {
	public static EnrollmentCreateResponse from(Enrollment enrollment) {
		return new EnrollmentCreateResponse(
			enrollment.getId(),
			enrollment.getCourseId(),
			enrollment.getStatus(),
			enrollment.getEnrolledAt()
		);
	}
}
