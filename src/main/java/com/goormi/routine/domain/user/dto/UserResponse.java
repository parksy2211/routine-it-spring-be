package com.goormi.routine.domain.user.dto;

import lombok.Builder;

@Builder
public record UserResponse(
	Long id,
	String nickname,
	String profileMessage,
	String profileImageUrl,
	Boolean isAlarmOn,
	Boolean isDarkMode
) {}
