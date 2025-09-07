package com.goormi.routine.domain.user.dto;

import lombok.Builder;

@Builder
public record UserRequest(
	String nickname,
	String profileMessage,
	String profileImageUrl
) {}
