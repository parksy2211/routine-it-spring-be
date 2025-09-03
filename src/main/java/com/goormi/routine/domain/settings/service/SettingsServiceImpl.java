package com.goormi.routine.domain.settings.service;

import org.springframework.stereotype.Service;

import com.goormi.routine.domain.settings.dto.SettingsRequest;
import com.goormi.routine.domain.settings.dto.SettingsResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SettingsServiceImpl implements SettingsService{

	@Override
	public SettingsResponse getSettings(Long userId) {
		return null;
	}

	@Override
	public SettingsResponse updateSettings(Long userId, SettingsRequest request) {
		return null;
	}

	@Override
	public SettingsResponse createDefaultSettings(Long userId) {
		return null;
	}

	@Override
	public void deleteSettings(Long userId) {

	}
}
