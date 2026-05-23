package com.example.course.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.example.course.domain.Course;
import com.example.course.domain.CourseStatus;
import com.example.course.dto.request.CourseCreateRequest;
import com.example.course.dto.response.CourseResponse;
import com.example.course.exception.CourseError;
import com.example.course.repository.CourseRepository;
import com.example.global.exception.BusinessException;
import com.example.user.domain.Role;
import com.example.user.domain.User;
import com.example.user.exception.UserError;
import com.example.user.repository.UserRepository;

@SpringBootTest
@Transactional
class CourseServiceTest {

	@Autowired
	private CourseService courseService;

	@Autowired
	private CourseRepository courseRepository;

	@Autowired
	private UserRepository userRepository;

	private User teacher;

	@BeforeEach
	void setup() {
		teacher = User.create("김선생", "teacher1@email.com", Role.TEACHER);
		userRepository.save(teacher);
	}

	@Nested
	@DisplayName("강의 등록 테스트")
	class CreateCourseTest {

		@Test
		@DisplayName("강의 등록에 성공한다")
		void createCourse_success() {
			// given
			CourseCreateRequest request = new CourseCreateRequest(
				"스프링 부트 입문",
				"스프링 부트 백엔드 개발 강의입니다.",
				new BigDecimal("50000"),
				30,
				LocalDate.of(2026, 6, 1),
				LocalDate.of(2026, 8, 31)
			);

			// when
			Long courseId = courseService.createCourse(teacher.getId(), request);

			// then
			assertThat(courseId).isNotNull();

			Course savedCourse = courseRepository.findById(courseId).orElseThrow();
			assertThat(savedCourse.getTeacherId()).isEqualTo(teacher.getId());
			assertThat(savedCourse.getTitle()).isEqualTo("스프링 부트 입문");
			assertThat(savedCourse.getDescription()).isEqualTo("스프링 부트 백엔드 개발 강의입니다.");
			assertThat(savedCourse.getPrice()).isEqualByComparingTo("50000");
			assertThat(savedCourse.getCapacity()).isEqualTo(30);
			assertThat(savedCourse.getEnrolledCount()).isZero();
			assertThat(savedCourse.getStartDate()).isEqualTo(LocalDate.of(2026, 6, 1));
			assertThat(savedCourse.getEndDate()).isEqualTo(LocalDate.of(2026, 8, 31));
			assertThat(savedCourse.getStatus()).isEqualTo(CourseStatus.DRAFT);
		}

		@Test
		@DisplayName("종료일이 시작일보다 앞서면 강의 등록에 실패한다")
		void createCourse_fail_invalidPeriod() {
			// given
			CourseCreateRequest request = new CourseCreateRequest(
				"잘못된 기간 강의",
				"기간 검증 테스트",
				new BigDecimal("30000"),
				20,
				LocalDate.of(2026, 9, 1),
				LocalDate.of(2026, 7, 1)
			);

			// when & then
			assertThatThrownBy(() -> courseService.createCourse(teacher.getId(), request))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("errorCode", CourseError.INVALID_PERIOD);

			assertThat(courseRepository.count()).isZero();
		}

		@Test
		@DisplayName("존재하지 않는 강사 ID로 강의 등록 시 실패한다")
		void createCourse_fail_userNotFound() {
			// given
			Long nonExistentTeacherId = 999L;
			CourseCreateRequest request = new CourseCreateRequest(
				"존재하지 않는 강사 강의",
				"존재하지 않는 강사로 등록 시도",
				new BigDecimal("50000"),
				30,
				LocalDate.of(2026, 6, 1),
				LocalDate.of(2026, 8, 31)
			);

			// when & then
			assertThatThrownBy(() -> courseService.createCourse(nonExistentTeacherId, request))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("errorCode", UserError.USER_NOT_FOUND);

			assertThat(courseRepository.count()).isZero();
		}

		@Test
		@DisplayName("강사 권한이 없는 사용자가 강의 등록 시 실패한다")
		void createCourse_fail_notTeacher() {
			// given
			User student = userRepository.save(User.create("홍학생", "student1@email.com", Role.STUDENT));
			CourseCreateRequest request = new CourseCreateRequest(
				"학생이 등록 시도하는 강의",
				"학생 권한으로 등록 시도",
				new BigDecimal("50000"),
				30,
				LocalDate.of(2026, 6, 1),
				LocalDate.of(2026, 8, 31)
			);

			// when & then
			assertThatThrownBy(() -> courseService.createCourse(student.getId(), request))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("errorCode", UserError.FORBIDDEN_TEACHER_ONLY);

			assertThat(courseRepository.count()).isZero();
		}
	}

	@Nested
	@DisplayName("강의 상세 조회 테스트")
	class GetCourseTest {

		@Test
		@DisplayName("강의 상세 조회에 성공한다")
		void getCourse_success() {
			// given
			Course course = courseRepository.save(Course.create(
				teacher.getId(),
				"스프링 부트 입문",
				"스프링 부트 백엔드 개발 강의입니다.",
				new BigDecimal("50000"),
				30,
				LocalDate.of(2026, 6, 1),
				LocalDate.of(2026, 8, 31)
			));

			// when
			CourseResponse response = courseService.getCourse(course.getId());

			// then
			assertThat(response.id()).isEqualTo(course.getId());
			assertThat(response.teacherId()).isEqualTo(teacher.getId());
			assertThat(response.title()).isEqualTo("스프링 부트 입문");
			assertThat(response.description()).isEqualTo("스프링 부트 백엔드 개발 강의입니다.");
			assertThat(response.price()).isEqualByComparingTo("50000");
			assertThat(response.capacity()).isEqualTo(30);
			assertThat(response.enrolledCount()).isZero();
			assertThat(response.startDate()).isEqualTo(LocalDate.of(2026, 6, 1));
			assertThat(response.endDate()).isEqualTo(LocalDate.of(2026, 8, 31));
			assertThat(response.status()).isEqualTo(CourseStatus.DRAFT);
		}

		@Test
		@DisplayName("존재하지 않는 강의 ID로 조회 시 실패한다")
		void getCourse_fail_notFound() {
			// given
			Long nonExistentCourseId = 999L;

			// when & then
			assertThatThrownBy(() -> courseService.getCourse(nonExistentCourseId))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("errorCode", CourseError.NOT_FOUND_COURSE);
		}
	}


}