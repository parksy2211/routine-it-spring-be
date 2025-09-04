package com.goormi.routine.domain.settings.controller;

import com.goormi.routine.common.response.ApiResponse;
import com.goormi.routine.domain.settings.dto.SettingsRequest;
import com.goormi.routine.domain.settings.dto.SettingsResponse;
import com.goormi.routine.domain.settings.service.SettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "설정", description = "사용자 설정 관련 API")
@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class SettingsController {

	private final SettingsService settingsService;

	@Operation(summary = "설정 조회", description = "사용자의 설정 정보를 조회합니다")
	@GetMapping
	public ApiResponse<SettingsResponse> getSettings(@AuthenticationPrincipal Long userId) {
		return ApiResponse.success(settingsService.getSettings(userId));
	}
	@Operation(summary = "알림 설정 토글", description = "알림 설정을 켜고/끕니다")
	@PatchMapping("/alarm")
	public ApiResponse<Boolean> toggleAlarm(
		@AuthenticationPrincipal Long userId,
		@RequestParam Boolean enabled
	) {
		return ApiResponse.success(settingsService.updateAlarmSetting(userId, enabled));
	}

	@Operation(summary = "다크모드 설정 토글", description = "다크모드를 켜고/끕니다")
	@PatchMapping("/dark-mode")
	public ApiResponse<Boolean> toggleDarkMode(
		@AuthenticationPrincipal Long userId,
		@RequestParam Boolean enabled
	) {
		return ApiResponse.success(settingsService.updateDarkModeSetting(userId, enabled));
	}

	@Operation(summary = "설정 초기화", description = "사용자 설정을 기본값으로 초기화합니다")
	@PostMapping("/reset")
	public ApiResponse<SettingsResponse> resetSettings(@AuthenticationPrincipal Long userId) {
		return ApiResponse.success(settingsService.resetSettings(userId));
	}
}