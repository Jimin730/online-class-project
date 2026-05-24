package com.example.course.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.example.course.exception.CourseError;
import com.example.global.domain.BaseEntity;
import com.example.global.exception.DomainException;

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
public class Course extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private Long teacherId;

	@Column(nullable = false)
	private String title;

	@Column(columnDefinition = "TEXT", nullable = false)
	private String description;

	@Column(nullable = false)
	private BigDecimal price;

	@Column(nullable = false)
	private int capacity;

	@Column(nullable = false)
	private int enrolledCount;

	@Column(nullable = false)
	private LocalDate startDate;

	@Column(nullable = false)
	private LocalDate endDate;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private CourseStatus status;

	public static Course create(
		Long teacherId,
		String title,
		String description,
		BigDecimal price,
		int capacity,
		LocalDate startDate,
		LocalDate endDate
	) {
		Course course = new Course();
		course.teacherId = teacherId;
		course.title = title;
		course.description = description;
		course.price = price;
		course.capacity = capacity;
		course.enrolledCount = 0;
		course.startDate = startDate;
		course.endDate = endDate;
		course.status = CourseStatus.DRAFT;
		return course;
	}

	public void enroll() {
		if (this.status != CourseStatus.OPEN) {
			throw new DomainException(CourseError.NOT_OPEN);
		}
		if (this.enrolledCount >= this.capacity) {
			throw new DomainException(CourseError.CAPACITY_EXCEEDED);
		}
		this.enrolledCount++;
	}

	public void release() {
		if (this.enrolledCount > 0) {
			this.enrolledCount--;
		}
	}

	public void open() {
		if(this.status != CourseStatus.DRAFT) {
			throw new DomainException(CourseError.CANNOT_OPEN);
		}
		this.status = CourseStatus.OPEN;
	}

	public void close() {
		if(this.status != CourseStatus.OPEN) {
			throw new DomainException(CourseError.CANNOT_CLOSED);
		}
		this.status = CourseStatus.CLOSED;
	}
}
