package com.goormi.routine.kakao;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

@Service
public class UserKakaoTokenService {
    // TODO: 실제 구현 - DB에서 사용자별 accessToken/refreshToken 관리 및 만료 시 갱신
    public String getValidAccessToken(Integer userId) {
        // 1) DB 조회
        // 2) 만료 체크 → refresh로 갱신
        // 3) 유효 액세스 토큰 반환
        return "USER_ACCESS_TOKEN_FROM_DB";
    }
}