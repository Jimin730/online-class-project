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
import com.example.enrollment.domain.Enrollment;
import com.example.enrollment.domain.EnrollmentStatus;
import com.example.enrollment.dto.response.EnrollmentCreateResponse;
import com.example.enrollment.exception.EnrollmentError;
import com.example.enrollment.repository.EnrollmentRepository;
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

	@Autowired
	private EnrollmentRepository enrollmentRepository;

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

	@Nested
	@DisplayName("수강신청 확정 테스트")
	class ConfirmTest {

		@Test
		@DisplayName("본인의 PENDING 상태 수강신청을 확정한다")
		void confirm_success() {
			// given
			Enrollment enrollment = enrollmentRepository.save(
				Enrollment.create(student.getId(), course1.getId())
			);

			// when
			enrollmentService.confirmEnrollment(student.getId(), enrollment.getId());

			// then
			Enrollment confirmedEnrollment = enrollmentRepository.findById(enrollment.getId()).orElseThrow();
			assertThat(confirmedEnrollment.getStatus()).isEqualTo(EnrollmentStatus.CONFIRMED);
			assertThat(confirmedEnrollment.getConfirmedAt()).isNotNull();
		}

		@Test
		@DisplayName("존재하지 않는 수강신청 ID로 확정 시 실패한다")
		void confirm_fail_notFound() {
			// given
			Long nonExistentEnrollmentId = 999L;

			// when & then
			assertThatThrownBy(() -> enrollmentService.confirmEnrollment(student.getId(), nonExistentEnrollmentId))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("errorCode", EnrollmentError.NOT_FOUND_ENROLLMENT);
		}

		@Test
		@DisplayName("본인의 수강신청이 아니면 확정할 수 없다")
		void confirm_fail_notOwner() {
			// given
			User otherStudent = userRepository.save(User.create("이학생", "student2@email.com", Role.STUDENT));
			Enrollment enrollment = enrollmentRepository.save(
				Enrollment.create(student.getId(), course1.getId())
			);

			// when & then
			assertThatThrownBy(() -> enrollmentService.confirmEnrollment(otherStudent.getId(), enrollment.getId()))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("errorCode", EnrollmentError.NOT_OWNER);
		}

		@Test
		@DisplayName("PENDING 상태가 아닌 수강신청은 확정할 수 없다")
		void confirm_fail_notPending() {
			// given
			Enrollment enrollment = enrollmentRepository.save(
				Enrollment.create(student.getId(), course1.getId())
			);
			enrollment.confirm(); // 수강 확정 처리

			// when & then
			assertThatThrownBy(() -> enrollmentService.confirmEnrollment(student.getId(), enrollment.getId()))
				.isInstanceOf(DomainException.class)
				.hasFieldOrPropertyWithValue("errorCode", EnrollmentError.CANNOT_CONFIRM);
		}
	}

	@Nested
	@DisplayName("수강신청 취소 테스트")
	class CancelTest {

		@Test
		@DisplayName("본인의 수강신청을 취소하면 강의 신청 인원이 감소한다")
		void cancel_success() {
			// given
			course1.open();
			course1.enroll();
			Enrollment enrollment = enrollmentRepository.save(
				Enrollment.create(student.getId(), course1.getId())
			);

			// when
			enrollmentService.cancelEnrollment(student.getId(), enrollment.getId());

			// then
			Enrollment cancelledEnrollment = enrollmentRepository.findById(enrollment.getId()).orElseThrow();
			assertThat(cancelledEnrollment.getStatus()).isEqualTo(EnrollmentStatus.CANCELLED);
			assertThat(cancelledEnrollment.getCancelledAt()).isNotNull();

			Course updatedCourse = courseRepository.findById(course1.getId()).orElseThrow();
			assertThat(updatedCourse.getEnrolledCount()).isZero();
		}

		@Test
		@DisplayName("존재하지 않는 수강신청 ID로 취소 시 실패한다")
		void cancel_fail_notFound() {
			// given
			Long nonExistentEnrollmentId = 999L;

			// when & then
			assertThatThrownBy(() -> enrollmentService.cancelEnrollment(student.getId(), nonExistentEnrollmentId))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("errorCode", EnrollmentError.NOT_FOUND_ENROLLMENT);
		}

		@Test
		@DisplayName("본인의 수강신청이 아니면 취소할 수 없다")
		void cancel_fail_notOwner() {
			// given
			User otherStudent = userRepository.save(User.create("이학생", "student2@email.com", Role.STUDENT));
			Enrollment enrollment = enrollmentRepository.save(
				Enrollment.create(student.getId(), course1.getId())
			);

			// when & then
			assertThatThrownBy(() -> enrollmentService.cancelEnrollment(otherStudent.getId(), enrollment.getId()))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("errorCode", EnrollmentError.NOT_OWNER);
		}

		@Test
		@DisplayName("이미 취소된 수강신청은 다시 취소할 수 없다")
		void cancel_fail_alreadyCancelled() {
			// given
			Enrollment enrollment = enrollmentRepository.save(
				Enrollment.create(student.getId(), course1.getId())
			);
			enrollment.cancel();

			// when & then
			assertThatThrownBy(() -> enrollmentService.cancelEnrollment(student.getId(), enrollment.getId()))
				.isInstanceOf(DomainException.class)
				.hasFieldOrPropertyWithValue("errorCode", EnrollmentError.CANNOT_CANCEL);
		}
	}
}