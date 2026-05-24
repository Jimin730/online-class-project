package com.example.enrollment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.enrollment.domain.Enrollment;
import com.example.enrollment.domain.EnrollmentStatus;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

	boolean existsByStudentIdAndCourseIdAndStatusNot(Long studentId, Long courseId, EnrollmentStatus status);
}
