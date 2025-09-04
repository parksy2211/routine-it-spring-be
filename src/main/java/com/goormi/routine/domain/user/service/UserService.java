package com.goormi.routine.domain.user.service;

import com.goormi.routine.domain.user.dto.UserRequest;
import com.goormi.routine.domain.user.dto.UserResponse;

public interface UserService {
	UserResponse getMyProfile(Long userId);
	UserResponse updateProfile(Long userId, UserRequest request);
	UserResponse getUserProfile(Long userId);
	void deleteAccount(Long userId, String accessToken);
}
