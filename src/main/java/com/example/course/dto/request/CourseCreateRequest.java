package com.example.course.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CourseCreateRequest(

	@NotBlank(message = "제목은 필수입니다")
	@Size(max = 100, message = "제목은 100자 이내여야 합니다")
	String title,

	@NotBlank(message = "설명은 필수입니다")
	@Size(max = 2000, message = "설명은 2000자 이내여야 합니다")
	String description,

	@NotNull(message = "가격은 필수입니다")
	@DecimalMin(value = "0", message = "가격은 0 이상이어야 합니다")
	BigDecimal price,

	@Min(value = 1, message = "정원은 1명 이상이어야 합니다")
	int capacity,

	@NotNull(message = "시작일은 필수입니다")
	@FutureOrPresent(message = "시작일은 오늘 이후여야 합니다")
	LocalDate startDate,

	@NotNull(message = "종료일은 필수입니다")
	@Future(message = "종료일은 미래 날짜여야 합니다")
	LocalDate endDate

) {
}
