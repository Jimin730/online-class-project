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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.transaction.annotation.Transactional;

import com.example.course.domain.Course;
import com.example.course.domain.CourseStatus;
import com.example.course.dto.request.CourseCreateRequest;
import com.example.course.dto.response.CourseListResponse;
import com.example.course.dto.response.CourseResponse;
import com.example.course.exception.CourseError;
import com.example.course.repository.CourseRepository;
import com.example.global.exception.BusinessException;
import com.example.global.exception.DomainException;
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

	@Nested
	@DisplayName("강의 모집 시작 상태 변경 테스트")
	class ChangeCourseStatusTest {

		@Test
		@DisplayName("DRAFT 상태 강의를 OPEN에 성공한다")
		void openCourse_success() {
			// given
			Course course = courseRepository.save(Course.create(
				teacher.getId(),
				"스프링 부트 입문",
				"설명",
				new BigDecimal("50000"),
				30,
				LocalDate.of(2026, 6, 1),
				LocalDate.of(2026, 8, 31)
			));

			// when
			courseService.openCourse(teacher.getId(), course.getId());

			// then
			Course updatedCourse = courseRepository.findById(course.getId()).orElseThrow();
			assertThat(updatedCourse.getStatus()).isEqualTo(CourseStatus.OPEN);
		}

		@Test
		@DisplayName("DRAFT 상태가 아닌 강의를 OPEN 시 실패한다")
		void openCourse_fail_notDraft() {
			// given
			Course course = courseRepository.save(Course.create(
				teacher.getId(),
				"스프링 부트 입문",
				"설명",
				new BigDecimal("50000"),
				30,
				LocalDate.of(2026, 6, 1),
				LocalDate.of(2026, 8, 31)
			));
			course.open();

			// when & then
			assertThatThrownBy(() -> courseService.openCourse(teacher.getId(), course.getId()))
				.isInstanceOf(DomainException.class)
				.hasFieldOrPropertyWithValue("errorCode", CourseError.CANNOT_OPEN);
		}

		@Test
		@DisplayName("강의 소유자가 아닌 강사가 OPEN 시 실패한다")
		void openCourse_fail_notOwner() {
			// given
			Course course = courseRepository.save(Course.create(
				teacher.getId(),
				"스프링 부트 입문",
				"설명",
				new BigDecimal("50000"),
				30,
				LocalDate.of(2026, 6, 1),
				LocalDate.of(2026, 8, 31)
			));
			User otherTeacher = userRepository.save(User.create("박선생", "teacher2@email.com", Role.TEACHER));

			// when & then
			assertThatThrownBy(() -> courseService.openCourse(otherTeacher.getId(), course.getId()))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("errorCode", CourseError.NOT_COURSE_OWNER);

			Course unchangedCourse = courseRepository.findById(course.getId()).orElseThrow();
			assertThat(unchangedCourse.getStatus()).isEqualTo(CourseStatus.DRAFT);
		}

		@Test
		@DisplayName("존재하지 않는 강의 ID로 모집 시작 시 실패한다")
		void openCourse_fail_notFound() {
			// given
			Long nonExistentCourseId = 999L;

			// when & then
			assertThatThrownBy(() -> courseService.openCourse(teacher.getId(), nonExistentCourseId))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("errorCode", CourseError.NOT_FOUND_COURSE);
		}
	}

	@Nested
	@DisplayName("강의 모집 마감 상태 변경 테스트")
	class CloseCourseTest {

		@Test
		@DisplayName("OPEN 상태 강의를 모집 마감에 성공한다")
		void closeCourse_success() {
			// given
			Course course = courseRepository.save(Course.create(
				teacher.getId(),
				"스프링 부트 입문",
				"설명",
				new BigDecimal("50000"),
				30,
				LocalDate.of(2026, 6, 1),
				LocalDate.of(2026, 8, 31)
			));
			course.open();

			// when
			courseService.closeCourse(teacher.getId(), course.getId());

			// then
			Course updatedCourse = courseRepository.findById(course.getId()).orElseThrow();
			assertThat(updatedCourse.getStatus()).isEqualTo(CourseStatus.CLOSED);
		}

		@Test
		@DisplayName("DRAFT 상태 강의를 모집 마감 시 실패한다")
		void closeCourse_fail_fromDraft() {
			// given
			Course course = courseRepository.save(Course.create(
				teacher.getId(),
				"스프링 부트 입문",
				"설명",
				new BigDecimal("50000"),
				30,
				LocalDate.of(2026, 6, 1),
				LocalDate.of(2026, 8, 31)
			));

			// when & then
			assertThatThrownBy(() -> courseService.closeCourse(teacher.getId(), course.getId()))
				.isInstanceOf(DomainException.class)
				.hasFieldOrPropertyWithValue("errorCode", CourseError.CANNOT_CLOSED);
		}

		@Test
		@DisplayName("이미 마감된 강의를 다시 마감 시 실패한다")
		void closeCourse_fail_alreadyClosed() {
			// given
			Course course = courseRepository.save(Course.create(
				teacher.getId(),
				"스프링 부트 입문",
				"설명",
				new BigDecimal("50000"),
				30,
				LocalDate.of(2026, 6, 1),
				LocalDate.of(2026, 8, 31)
			));
			course.open();
			course.close();

			// when & then
			assertThatThrownBy(() -> courseService.closeCourse(teacher.getId(), course.getId()))
				.isInstanceOf(DomainException.class)
				.hasFieldOrPropertyWithValue("errorCode", CourseError.CANNOT_CLOSED);
		}

		@Test
		@DisplayName("강의 소유자가 아닌 강사가 모집 마감 시 실패한다")
		void closeCourse_fail_notOwner() {
			// given
			Course course = courseRepository.save(Course.create(
				teacher.getId(),
				"스프링 부트 입문",
				"설명",
				new BigDecimal("50000"),
				30,
				LocalDate.of(2026, 6, 1),
				LocalDate.of(2026, 8, 31)
			));
			course.open();
			User otherTeacher = userRepository.save(User.create("박선생", "teacher2@email.com", Role.TEACHER));

			// when & then
			assertThatThrownBy(() -> courseService.closeCourse(otherTeacher.getId(), course.getId()))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("errorCode", CourseError.NOT_COURSE_OWNER);

			Course unchangedCourse = courseRepository.findById(course.getId()).orElseThrow();
			assertThat(unchangedCourse.getStatus()).isEqualTo(CourseStatus.OPEN);
		}

		@Test
		@DisplayName("존재하지 않는 강의 ID로 모집 마감 시 실패한다")
		void closeCourse_fail_notFound() {
			// given
			Long nonExistentCourseId = 999L;

			// when & then
			assertThatThrownBy(() -> courseService.closeCourse(teacher.getId(), nonExistentCourseId))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("errorCode", CourseError.NOT_FOUND_COURSE);
		}
	}

	@Nested
	@DisplayName("강의 목록 조회 테스트")
	class GetCoursesTest {

		@Test
		@DisplayName("status가 null이면 OPEN/CLOSED 강의만 조회되고 DRAFT는 제외된다")
		void getCourses_visible_excludesDraft() {
			// given
			Course draftCourse = saveCourse("DRAFT 강의");
			Course openCourse = saveCourse("OPEN 강의");
			openCourse.open();
			Course closedCourse = saveCourse("CLOSED 강의");
			closedCourse.open();
			closedCourse.close();

			// when
			Slice<CourseListResponse> result = courseService.getCourses(null, PageRequest.of(0, 20));

			// then
			assertThat(result.getContent())
				.extracting(CourseListResponse::id)
				.containsExactlyInAnyOrder(openCourse.getId(), closedCourse.getId())
				.doesNotContain(draftCourse.getId());
		}

		@Test
		@DisplayName("status가 OPEN이면 OPEN 강의만 조회된다")
		void getCourses_filterByOpen() {
			// given
			Course draftCourse = saveCourse("DRAFT 강의");
			Course openCourse = saveCourse("OPEN 강의");
			openCourse.open();
			Course closedCourse = saveCourse("CLOSED 강의");
			closedCourse.open();
			closedCourse.close();

			// when
			Slice<CourseListResponse> result = courseService.getCourses(CourseStatus.OPEN, PageRequest.of(0, 20));

			// then
			assertThat(result.getContent())
				.extracting(CourseListResponse::id)
				.containsExactly(openCourse.getId())
				.doesNotContain(draftCourse.getId(), closedCourse.getId());
		}

		@Test
		@DisplayName("status가 DRAFT이면 조회할 수 없다")
		void getCourses_fail_draftStatus() {
			// when & then
			assertThatThrownBy(() -> courseService.getCourses(CourseStatus.DRAFT, PageRequest.of(0, 20)))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("errorCode", CourseError.INVALID_STATUS_FILTER);
		}

		@Test
		@DisplayName("페이지 크기를 초과하는 강의가 있으면 hasNext가 true이다")
		void getCourses_hasNext() {
			// given
			for (int i = 0; i < 3; i++) {
				Course course = saveCourse("강의 " + i);
				course.open();
			}

			// when
			Slice<CourseListResponse> result = courseService.getCourses(CourseStatus.OPEN, PageRequest.of(0, 2));

			// then
			assertThat(result.getContent()).hasSize(2);
			assertThat(result.hasNext()).isTrue();
		}

		private Course saveCourse(String title) {
			return courseRepository.save(Course.create(
				teacher.getId(),
				title,
				"설명",
				new BigDecimal("50000"),
				30,
				LocalDate.of(2026, 6, 1),
				LocalDate.of(2026, 8, 31)
			));
		}
	}
}