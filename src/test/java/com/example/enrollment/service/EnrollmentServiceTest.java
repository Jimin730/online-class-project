package com.example.enrollment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.example.course.domain.Course;
import com.example.course.exception.CourseError;
import com.example.course.repository.CourseRepository;
import com.example.enrollment.domain.EnrollmentStatus;
import com.example.enrollment.dto.response.EnrollmentCreateResponse;
import com.example.enrollment.exception.EnrollmentError;
import com.example.global.exception.BusinessException;
import com.example.global.exception.DomainException;
import com.example.user.domain.Role;
import com.example.user.domain.User;
import com.example.user.repository.UserRepository;

@SpringBootTest
@Transactional
class EnrollmentServiceTest {

	@Autowired
	private EnrollmentService enrollmentService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private CourseRepository courseRepository;

	private User teacher;
	private User student;
	private Course course1;

	@BeforeEach
	void setup() {
		teacher = User.create("김선생", "teacher1@email.com", Role.TEACHER);
		student = User.create("김학생", "student1@email.com", Role.STUDENT);
		userRepository.saveAll(List.of(teacher, student));

		course1 = Course.create(
			teacher.getId(),
			"스프링 부트 입문",
			"스프링 부트 백엔드 개발 강의입니다.",
			new BigDecimal("50000"),
			30,
			LocalDate.of(2026, 6, 1),
			LocalDate.of(2026, 8, 31)
		);
		courseRepository.save(course1);
	}

	@Nested
	@DisplayName("수강신청 테스트")
	class EnrollTest {

		@Test
		@DisplayName("학생이 수강신청에 성공한다")
		void enroll_success() {
			// given
			course1.open();

			// when
			EnrollmentCreateResponse response = enrollmentService.createEnrollment(student.getId(), course1.getId());

			// then
			assertThat(response.id()).isNotNull();
			assertThat(response.courseId()).isEqualTo(course1.getId());
			assertThat(response.status()).isEqualTo(EnrollmentStatus.PENDING);
			assertThat(response.enrolledAt()).isNotNull();

			Course updatedCourse = courseRepository.findById(course1.getId()).orElseThrow();
			assertThat(updatedCourse.getEnrolledCount()).isOne();
		}

		@Test
		@DisplayName("존재하지 않는 강의 ID로 수강신청 시 실패한다")
		void enroll_fail_courseNotFound() {
			// given
			Long nonExistentCourseId = 999L;

			// when & then
			assertThatThrownBy(() -> enrollmentService.createEnrollment(student.getId(), nonExistentCourseId))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("errorCode", CourseError.NOT_FOUND_COURSE);
		}

		@Test
		@DisplayName("본인이 개설한 강의는 수강신청할 수 없다")
		void enroll_fail_ownCourse() {
			// given
			course1.open();

			// when & then
			assertThatThrownBy(() -> enrollmentService.createEnrollment(teacher.getId(), course1.getId()))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("errorCode", EnrollmentError.CANNOT_ENROLL_OWN_COURSE);
		}

		@Test
		@DisplayName("이미 신청한 강의는 중복 신청할 수 없다")
		void enroll_fail_alreadyEnrolled() {
			// given
			course1.open();
			enrollmentService.createEnrollment(student.getId(), course1.getId());

			// when & then
			assertThatThrownBy(() -> enrollmentService.createEnrollment(student.getId(), course1.getId()))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("errorCode", EnrollmentError.ALREADY_ENROLLED);
		}

		@Test
		@DisplayName("모집중이 아닌 강의는 수강신청할 수 없다")
		void enroll_fail_courseNotOpen() {
			// given
			// course1은 DRAFT 상태로 생성됨

			// when & then
			assertThatThrownBy(() -> enrollmentService.createEnrollment(student.getId(), course1.getId()))
				.isInstanceOf(DomainException.class)
				.hasFieldOrPropertyWithValue("errorCode", CourseError.NOT_OPEN);
		}
	}
}