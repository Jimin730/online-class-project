package com.example.enrollment.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.course.domain.Course;
import com.example.course.exception.CourseError;
import com.example.course.repository.CourseRepository;
import com.example.enrollment.domain.Enrollment;
import com.example.enrollment.domain.EnrollmentStatus;
import com.example.enrollment.dto.response.EnrollmentCreateResponse;
import com.example.enrollment.exception.EnrollmentError;
import com.example.enrollment.repository.EnrollmentRepository;
import com.example.global.exception.BusinessException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class EnrollmentService {

	private final CourseRepository courseRepository;
	private final EnrollmentRepository enrollmentRepository;

	public EnrollmentCreateResponse createEnrollment(Long studentId, Long courseId) {
		// 1. 강의 락 획득
		Course course = courseRepository.findByIdForUpdate(courseId)
			.orElseThrow(() -> new BusinessException(CourseError.NOT_FOUND_COURSE));

		// 강사 본인 강의 검증
		if(course.getTeacherId().equals(studentId)) {
			throw new BusinessException(EnrollmentError.CANNOT_ENROLL_OWN_COURSE);
		}

		// 2. 중복 신청 차단
		if(enrollmentRepository.existsByStudentIdAndCourseIdAndStatusNot(studentId, courseId, EnrollmentStatus.CANCELLED)) {
			throw new BusinessException(EnrollmentError.ALREADY_ENROLLED);
		}

		// 3. 강의 신청인원 증가 및 상태 변경
		course.enroll(); // pending 상태도 신청인원 증가에 포함. 좀비상태의 pending은 10분 만료 등으로 관리 필요

		// 4. Enrollment 엔티티 생성
		Enrollment enrollment = enrollmentRepository.save(
			Enrollment.create(studentId, courseId)
		);

		return EnrollmentCreateResponse.from(enrollment);
	}

	public void confirm(Long studentId, Long enrollmentId) {
		Enrollment enrollment = findOwnedBy(studentId, enrollmentId);
		enrollment.confirm();
	}

	private Enrollment findOwnedBy(Long studentId, Long enrollmentId) {
		Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
			.orElseThrow(() -> new BusinessException(EnrollmentError.NOT_FOUND_ENROLLMENT));

		if (!enrollment.getStudentId().equals(studentId)) {
			throw new BusinessException(EnrollmentError.NOT_OWNER);
		}

		return enrollment;
	}

}
