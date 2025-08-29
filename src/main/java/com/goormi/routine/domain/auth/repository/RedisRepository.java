package com.goormi.routine.domain.auth.repository;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Repository
public class RedisRepository {
	private final StringRedisTemplate redisTemplate;

	private static final String LAST_RESET_MONTH_KEY = "RANKING_LAST_RESET_MONTH";
	private static final String REVIEW_KEY_PREFIX = "REVIEW:";

	public RedisRepository(StringRedisTemplate redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	public void saveRefreshToken(String userId, String token, long duration) {
		redisTemplate.opsForValue().set("RT:" + userId, token, duration, TimeUnit.MILLISECONDS);
	}

	public String getRefreshToken(String userId) {
		return redisTemplate.opsForValue().get("RT:" + userId);
	}

	public void deleteRefreshToken(String userId) {
		redisTemplate.delete("RT:" + userId);
	}

	public void saveBlackList(String token, long duration) {
		redisTemplate.opsForValue().set("BL:" + token, "blacklisted", duration, TimeUnit.MILLISECONDS);
	}

	public boolean isBlackListed(String token) {
		return redisTemplate.hasKey("BL:" + token);
	}

	public void saveLastResetMonth(String monthYear) {
		redisTemplate.opsForValue().set(LAST_RESET_MONTH_KEY, monthYear);
	}

	public String getLastResetMonth() {
		return redisTemplate.opsForValue().get(LAST_RESET_MONTH_KEY);
	}

	public void deleteLastResetMonth() {
		redisTemplate.delete(LAST_RESET_MONTH_KEY);
	}

	public boolean hasLastResetMonth() {
		return redisTemplate.hasKey(LAST_RESET_MONTH_KEY);
	}

	public void saveReviewData(String userId, String monthYear, String reviewData) {
		String key = REVIEW_KEY_PREFIX + userId + ":" + monthYear;
		redisTemplate.opsForValue().set(key, reviewData, 90, TimeUnit.DAYS);
	}

	public String getReviewData(String userId, String monthYear) {
		String key = REVIEW_KEY_PREFIX + userId + ":" + monthYear;
		return redisTemplate.opsForValue().get(key);
	}

	public List<String> getUserReviewKeys(String userId) {
		String pattern = REVIEW_KEY_PREFIX + userId + ":*";
		return redisTemplate.keys(pattern)
			.stream()
			.sorted()
			.collect(Collectors.toList());
	}

	public void deleteReviewData(String userId, String monthYear) {
		String key = REVIEW_KEY_PREFIX + userId + ":" + monthYear;
		redisTemplate.delete(key);
	}

	public void deleteAllUserReviewData(String userId) {
		String pattern = REVIEW_KEY_PREFIX + userId + ":*";
		redisTemplate.keys(pattern).forEach(redisTemplate::delete);
	}

	public boolean hasReviewData(String userId, String monthYear) {
		String key = REVIEW_KEY_PREFIX + userId + ":" + monthYear;
		return redisTemplate.hasKey(key);
	}
}
