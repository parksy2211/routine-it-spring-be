package com.goormi.routine.domain.settings.service;

import com.goormi.routine.domain.settings.dto.SettingsRequest;
import com.goormi.routine.domain.settings.dto.SettingsResponse;

public interface SettingsService {
	SettingsResponse getSettings(Long userId);
	SettingsResponse updateSettings(Long userId, SettingsRequest request);
	SettingsResponse createDefaultSettings(Long userId);
	void deleteSettings(Long userId);
}
