package com.goormi.routine.domain.settings.service;

import com.goormi.routine.domain.settings.dto.SettingsResponse;

public interface SettingsService {
	SettingsResponse getSettings(Long userId);
	SettingsResponse resetSettings(Long userId);

	Boolean updateAlarmSetting(Long userId, Boolean isAlarmOn);
	Boolean updateDarkModeSetting(Long userId, Boolean isDarkMode);
}
