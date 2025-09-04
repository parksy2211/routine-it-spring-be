package com.goormi.routine.domain.settings.dto;

import lombok.Builder;

@Builder
public record SettingsRequest(
	Boolean isAlarmOn,
	Boolean isDarkMode
) {}