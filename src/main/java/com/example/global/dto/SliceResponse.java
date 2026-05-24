package com.example.global.dto;

import java.util.List;

import org.springframework.data.domain.Slice;

public record SliceResponse<T>(
	List<T> content,
	int size,
	boolean hasNext
) {
	public static <T> SliceResponse<T> from(Slice<T> slice) {
		return new SliceResponse<>(
			slice.getContent(),
			slice.getSize(),
			slice.hasNext()
		);
	}
}
