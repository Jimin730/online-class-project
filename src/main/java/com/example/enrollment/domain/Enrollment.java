package com.example.enrollment.domain;

import java.time.LocalDateTime;

import com.example.global.domain.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Enrollment extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private Long studentId;

	@Column(nullable = false)
	private Long courseId;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private EnrollmentStatus status;

	@Column(nullable = false)
	private LocalDateTime enrolledAt;

	private LocalDateTime confirmedAt;

	private LocalDateTime cancelledAt;

	public static Enrollment create(
		Long studentId,
		Long courseId
	) {
		Enrollment enrollment = new Enrollment();
		enrollment.studentId = studentId;
		enrollment.courseId = courseId;
		enrollment.status = EnrollmentStatus.PENDING;
		enrollment.enrolledAt = LocalDateTime.now();
		return enrollment;
	}
}
