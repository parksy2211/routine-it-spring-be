package com.goormi.routine.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RedisKakaoTokenStore implements KakaoTokenStore {

    private static final String KEY_PREFIX = "kakao:token:";

    private final StringRedisTemplate redis;

    @Override
    public void save(Long userId, String accessToken, Duration ttl) {
        String key = KEY_PREFIX + userId;
        if (ttl == null || ttl.isNegative() || ttl.isZero()) {
            ttl = Duration.ofHours(6);
        }
        redis.opsForValue().set(key, accessToken, ttl);
    }

    @Override
    public String get(Long userId) {
        return redis.opsForValue().get(KEY_PREFIX + userId);
    }

    @Override
    public void delete(Long userId) {
        redis.delete(KEY_PREFIX + userId);
    }
}
