package com.goormi.routine.domain.auth.repository;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

@Repository
public class RedisRepository {
	private final StringRedisTemplate redisTemplate;

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

	private static final String LAST_RESET_MONTH_KEY = "RANKING_LAST_RESET_MONTH";

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

}
