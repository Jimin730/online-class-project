package com.example.course.repository;

import java.util.Optional;

import org.hibernate.cfg.AvailableSettings;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.course.domain.Course;
import com.example.course.domain.CourseStatus;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

	// open 우선 + 최신순, draft 제외
	@Query(value = """
        SELECT * FROM course
        WHERE status IN ('OPEN', 'CLOSED')
        ORDER BY
            CASE status WHEN 'OPEN' THEN 0 ELSE 1 END,
            created_at DESC
        """,
		nativeQuery = true)
	Slice<Course> findVisibleCourses(Pageable pageable);

	// 상태 단일 필터
	Slice<Course> findAllByStatus(CourseStatus status, Pageable pageable);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@QueryHints(@QueryHint(name = AvailableSettings.JAKARTA_LOCK_TIMEOUT, value = "3000")) // 3초 타임아웃
	@Query("SELECT c FROM Course c WHERE c.id = :id")
	Optional<Course> findByIdForUpdate(@Param("id") Long id);
}
