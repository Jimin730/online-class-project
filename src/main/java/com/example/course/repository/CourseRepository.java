package com.example.course.repository;

import java.util.Optional;

import org.hibernate.cfg.AvailableSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.course.domain.Course;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@QueryHints(@QueryHint(name = AvailableSettings.JAKARTA_LOCK_TIMEOUT, value = "3000")) // 3초 타임아웃
	@Query("SELECT c FROM Course c WHERE c.id = :id")
	Optional<Course> findByIdForUpdate(@Param("id") Long id);
}
