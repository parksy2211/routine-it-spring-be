package com.goormi.routine.domain.auth.service;

import java.time.Duration;

public interface KakaoTokenStore {
    void save(Long userId, String accessToken, Duration ttl);
    String get(Long userId);
    void delete(Long userId);
}