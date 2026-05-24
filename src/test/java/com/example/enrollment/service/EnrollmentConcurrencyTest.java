package com.example.enrollment.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.course.domain.Course;
import com.example.course.repository.CourseRepository;
import com.example.enrollment.domain.EnrollmentStatus;
import com.example.enrollment.repository.EnrollmentRepository;
import com.example.user.domain.Role;
import com.example.user.domain.User;
import com.example.user.repository.UserRepository;

@SpringBootTest
public class EnrollmentConcurrencyTest {

	@Autowired
	private EnrollmentService enrollmentService;

	@Autowired
	private CourseRepository courseRepository;

	@Autowired
	private EnrollmentRepository enrollmentRepository;

	@Autowired
	private UserRepository userRepository;

	@AfterEach
	void cleanup() {
		enrollmentRepository.deleteAllInBatch();
		courseRepository.deleteAllInBatch();
		userRepository.deleteAllInBatch();
	}

	@Test
	@DisplayName("마지막 한 자리에 50명이 동시 신청 시 1명만 신청에 성공한다")
	void enroll_concurrentLastSeat_onlyOneSucceeds() throws InterruptedException {
		// given
		// 강사 + 강의(capacity=1, 자리 1개) + 학생 50명
		User teacher = userRepository.save(
			User.create("김선생", "teacher@email.com", Role.TEACHER)
		);

		Course course = Course.create(
			teacher.getId(),
			"동시성 테스트 강의",
			"마지막 자리 경쟁",
			new BigDecimal("50000"),
			1,                           // ← capacity = 1
			LocalDate.of(2026, 7, 1),
			LocalDate.of(2026, 8, 31)
		);
		course.open();                   // OPEN 상태로 전환
		courseRepository.save(course);
		Long courseId = course.getId();

		int threadCount = 50;
		List<Long> studentIds = new ArrayList<>();
		for (int i = 0; i < threadCount; i++) {
			User student = userRepository.save(
				User.create("학생" + i, "student" + i + "@email.com", Role.STUDENT)
			);
			studentIds.add(student.getId());
		}

		// when
		// 50명이 동시에 신청
		ExecutorService executor = Executors.newFixedThreadPool(threadCount); // 스레드 풀 생성
		CountDownLatch readyLatch = new CountDownLatch(threadCount);
		CountDownLatch startLatch = new CountDownLatch(1);
		CountDownLatch doneLatch  = new CountDownLatch(threadCount);

		AtomicInteger successCount = new AtomicInteger(0);
		AtomicInteger failCount    = new AtomicInteger(0);

		for (Long studentId : studentIds) {
			executor.submit(() -> {
				try {
					readyLatch.countDown();
					startLatch.await(); // 모든 스레드 대기

					enrollmentService.createEnrollment(studentId, courseId);
					successCount.incrementAndGet();
				} catch (Exception e) {
					failCount.incrementAndGet();
				} finally {
					doneLatch.countDown();
				}
			});
		}

		readyLatch.await();
		startLatch.countDown(); // 모든 스레드 동시 시작
		boolean finished = doneLatch.await(30, TimeUnit.SECONDS); // 30초 안에 모든 워커가 못 끝내면 타임아웃 (무한 대기 방지)
		executor.shutdown();

		// then
		// 1명 성공, 49명 실패 확인
		assertThat(finished).as("타임아웃 발생").isTrue();
		assertThat(successCount.get()).isEqualTo(1);
		assertThat(failCount.get()).isEqualTo(49);

		// Course 상태 검증
		Course result = courseRepository.findById(courseId).orElseThrow();
		assertThat(result.getEnrolledCount()).isEqualTo(1);

		// Enrollment 수량 검증
		long enrollments = enrollmentRepository.findAll().stream()
			.filter(e -> e.getCourseId().equals(courseId))
			.filter(e -> e.getStatus() == EnrollmentStatus.PENDING)
			.count();
		assertThat(enrollments).isEqualTo(1);
	}
}
