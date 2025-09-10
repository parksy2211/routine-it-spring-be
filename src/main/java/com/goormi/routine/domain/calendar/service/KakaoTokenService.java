package com.goormi.routine.domain.calendar.service;

import com.goormi.routine.domain.user.entity.User;
import com.goormi.routine.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * 카카오 토큰 관리 서비스
 * 김영한 스타일: 외부 API 의존성 관리를 별도 서비스로 분리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoTokenService {

    private final WebClient webClient;
    private final UserRepository userRepository;

    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${kakao.client-secret}")
    private String clientSecret;

    /**
     * 카카오 Refresh Token으로 새로운 Access Token 발급
     */
    public String getKakaoAccessTokenByUserId(Long userId) {
        log.debug("카카오 액세스 토큰 조회 시작: userId={}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));
        
        if (user.getKakaoRefreshToken() == null) {
            throw new IllegalStateException("저장된 카카오 리프레시 토큰이 없습니다: " + userId);
        }
        
        return refreshKakaoAccessToken(user.getKakaoRefreshToken());
    }

    /**
     * 카카오 Refresh Token으로 Access Token 갱신
     */
    private String refreshKakaoAccessToken(String kakaoRefreshToken) {
        try {
            TokenResponse response = webClient.post()
                    .uri("https://kauth.kakao.com/oauth/token")
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                    .bodyValue(buildRefreshTokenRequest(kakaoRefreshToken))
                    .retrieve()
                    .bodyToMono(TokenResponse.class)
                    .block();

            if (response == null || response.accessToken == null) {
                throw new RuntimeException("카카오 토큰 갱신에 실패했습니다");
            }

            log.debug("카카오 액세스 토큰 갱신 성공");
            return response.accessToken;

        } catch (Exception e) {
            log.error("카카오 액세스 토큰 갱신 실패: {}", e.getMessage());
            throw new RuntimeException("카카오 액세스 토큰 갱신에 실패했습니다", e);
        }
    }

    /**
     * 토큰 갱신 요청 파라미터 구성
     */
    private String buildRefreshTokenRequest(String refreshToken) {
        return String.format(
                "grant_type=refresh_token&client_id=%s&client_secret=%s&refresh_token=%s",
                clientId, clientSecret, refreshToken
        );
    }

    /**
     * 카카오 토큰 응답 DTO
     */
    public static class TokenResponse {
        public String accessToken;
        public String refreshToken;
        public Integer expiresIn;
        public Integer refreshTokenExpiresIn;
    }
}
