package com.example.course.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.course.domain.Course;
import com.example.course.domain.CourseStatus;
import com.example.course.dto.request.CourseCreateRequest;
import com.example.course.dto.response.CourseListResponse;
import com.example.course.dto.response.CourseResponse;
import com.example.course.exception.CourseError;
import com.example.course.repository.CourseRepository;
import com.example.global.exception.BusinessException;
import com.example.user.domain.Role;
import com.example.user.domain.User;
import com.example.user.exception.UserError;
import com.example.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CourseService {

	private final CourseRepository courseRepository;
	private final UserRepository userRepository;

	public Long createCourse(Long teacherId, CourseCreateRequest request) {
		User teacher = userRepository.findById(teacherId)
			.orElseThrow(() -> new BusinessException(UserError.USER_NOT_FOUND));

		if (teacher.getRole() != Role.TEACHER) {
			throw new BusinessException(UserError.FORBIDDEN_TEACHER_ONLY);
		}

		if (request.endDate().isBefore(request.startDate())) {
			throw new BusinessException(CourseError.INVALID_PERIOD);
		}

		Course course = Course.create(
			teacherId,
			request.title(),
			request.description(),
			request.price(),
			request.capacity(),
			request.startDate(),
			request.endDate()
		);

		return courseRepository.save(course).getId();
	}

	@Transactional(readOnly = true)
	public CourseResponse getCourse(Long courseId) {
		Course course = courseRepository.findById(courseId)
			.orElseThrow(() -> new BusinessException(CourseError.NOT_FOUND_COURSE));

		return CourseResponse.from(course);
	}

	@Transactional(readOnly = true)
	public Slice<CourseListResponse> getCourses(CourseStatus status, Pageable pageable) {
		Slice<Course> courses;

		if (status == null) {
			Pageable page = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
			courses = courseRepository.findVisibleCourses(page);

		} else if (status == CourseStatus.DRAFT) { // 초안 조회 불가
			throw new BusinessException(CourseError.INVALID_STATUS_FILTER);

		} else {
			courses = courseRepository.findAllByStatus(status, pageable);
		}

		return courses.map(CourseListResponse::from);
	}

	public void openCourse(Long teacherId, Long courseId) {
		Course course = findCourseOwnedBy(courseId, teacherId);
		course.open();
	}

	public void closeCourse(Long teacherId, Long courseId) {
		Course course = findCourseOwnedBy(courseId, teacherId);
		course.close();
	}

	private Course findCourseOwnedBy(Long courseId, Long teacherId) {
		Course course = courseRepository.findById(courseId)
			.orElseThrow(() -> new BusinessException(CourseError.NOT_FOUND_COURSE));

		if (!course.getTeacherId().equals(teacherId)) {
			throw new BusinessException(CourseError.NOT_COURSE_OWNER);
		}
		return course;
	}

}