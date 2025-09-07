package com.goormi.routine.domain.settings.dto;

import lombok.Builder;

@Builder
public record SettingsResponse(
	Long userId,
	Boolean isAlarmOn,
	Boolean isDarkMode
) {}