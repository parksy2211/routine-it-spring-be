package com.goormi.routine.domain.user.service;

import com.goormi.routine.domain.auth.repository.RedisRepository;
import com.goormi.routine.domain.auth.service.JwtTokenProvider;
import com.goormi.routine.domain.user.dto.UserRequest;
import com.goormi.routine.domain.user.dto.UserResponse;
import com.goormi.routine.domain.user.entity.User;
import com.goormi.routine.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;
	private final RedisRepository redisRepository;
	private final JwtTokenProvider jwtTokenProvider;

	@Override
	public UserResponse getMyProfile(Long userId) {
		User user = userRepository.findById(userId).orElseThrow();
		return toResponse(user);
	}

	@Override
	public UserResponse updateProfile(Long userId, UserRequest request) {
		User user = userRepository.findById(userId).orElseThrow();
		user.updateProfile(request.nickname(), request.profileMessage(), request.profileImageUrl());
		userRepository.save(user);
		return toResponse(user);
	}

	@Override
	public UserResponse updateSettings(Long userId, UserRequest request) {
		User user = userRepository.findById(userId).orElseThrow();
		user.updateSettings(request.isAlarmOn(), request.isDarkMode());

		userRepository.save(user);

		return toResponse(user);
	}

	@Override
	public UserResponse getUserProfile(Long userId) {
		User user = userRepository.findById(userId).orElseThrow();
		return toResponse(user);
	}

	@Override
	public void deleteAccount(Long userId, String accessToken) {
		User user = userRepository.findById(userId).orElseThrow();

		user.updateRefreshToken(null);
		user.setActive(false);

		userRepository.save(user);

		long expiration = jwtTokenProvider.getRemainingExpiration(accessToken);
		redisRepository.saveBlackList(accessToken, expiration);

		redisRepository.deleteRefreshToken(String.valueOf(userId));
	}


	private UserResponse toResponse(User user) {
		return UserResponse.builder()
			.id(user.getId())
			.nickname(user.getNickname())
			.profileMessage(user.getProfileMessage())
			.profileImageUrl(user.getProfileImageUrl())
			.isAlarmOn(true)
			.isDarkMode(false)
			.build();
	}
}
