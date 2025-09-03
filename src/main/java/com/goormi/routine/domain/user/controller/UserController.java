package com.goormi.routine.domain.user.controller;

import com.goormi.routine.common.response.ApiResponse;
import com.goormi.routine.domain.user.dto.UserRequest;
import com.goormi.routine.domain.user.dto.UserResponse;
import com.goormi.routine.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "사용자", description = "사용자 프로필 및 설정 관련 API")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	@Operation(summary = "내 프로필 조회", description = "로그인된 사용자의 프로필 정보를 조회합니다")
	@GetMapping("/me")
	public ApiResponse<UserResponse> getMyProfile(@AuthenticationPrincipal Long userId) {
		return ApiResponse.success(userService.getMyProfile(userId));
	}

	@Operation(summary = "프로필 편집", description = "닉네임, 자기소개, 프로필 이미지를 수정합니다")
	@PutMapping("/me/profile")
	public ApiResponse<UserResponse> updateProfile(
		@AuthenticationPrincipal Long userId,
		@RequestBody UserRequest request
	) {
		return ApiResponse.success(userService.updateProfile(userId, request));
	}

	@Operation(summary = "타 사용자 프로필 조회", description = "특정 사용자의 공개 프로필 정보를 조회합니다")
	@GetMapping("/{userId}")
	public ApiResponse<UserResponse> getUserProfile(@PathVariable Long userId) {
		return ApiResponse.success(userService.getUserProfile(userId));
	}

	@Operation(summary = "회원 탈퇴", description = "로그인된 사용자의 계정을 삭제(비활성화)합니다")
	@DeleteMapping("/signout")
	public ApiResponse<Void> deleteAccount(@AuthenticationPrincipal Long userId, String accessToken) {
		userService.deleteAccount(userId, accessToken);
		return ApiResponse.success(null);
	}
}
