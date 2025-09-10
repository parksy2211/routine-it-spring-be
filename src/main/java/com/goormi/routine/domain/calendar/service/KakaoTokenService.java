package com.goormi.routine.domain.calendar.service;

import com.goormi.routine.domain.user.entity.User;
import com.goormi.routine.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
@ConditionalOnProperty(name = "calendar.integration.enabled", havingValue = "true", matchIfMissing = true)
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
        log.info("=== 카카오 액세스 토큰 조회 시작 ===");
        log.debug("요청 userId: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));
        
        log.debug("사용자 조회 완료: email={}", user.getEmail());
        
        if (user.getKakaoRefreshToken() == null) {
            log.error("저장된 카카오 리프레시 토큰이 없습니다: userId={}", userId);
            throw new IllegalStateException("저장된 카카오 리프레시 토큰이 없습니다: " + userId);
        }
        
        log.debug("카카오 리프레시 토큰 존재 확인 완료");
        log.debug("Using Kakao Refresh Token: {}", user.getKakaoRefreshToken());
        
        String accessToken = refreshKakaoAccessToken(user.getKakaoRefreshToken());
        log.info("카카오 액세스 토큰 조회 완료: userId={}", userId);
        
        return accessToken;
    }

    /**
     * 카카오 Refresh Token으로 Access Token 갱신
     */
    private String refreshKakaoAccessToken(String kakaoRefreshToken) {
        log.info("=== 카카오 액세스 토큰 갱신 시작 ===");
        log.debug("Client ID: {}", clientId);
        log.debug("Client Secret 존재 여부: {}", clientSecret != null && !clientSecret.trim().isEmpty());
        log.debug("Refresh Token 존재 여부: {}", kakaoRefreshToken != null && !kakaoRefreshToken.trim().isEmpty());
        
        try {
            log.debug("카카오 토큰 갱신 API 호출 시작");
            
            TokenResponse response = webClient.post()
                    .uri("https://kauth.kakao.com/oauth/token")
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                    .bodyValue(buildRefreshTokenRequest(kakaoRefreshToken))
                    .retrieve()
                    .bodyToMono(TokenResponse.class)
                    .block();

            log.debug("카카오 토큰 갱신 API 응답 수신");

            if (response == null || response.accessToken == null) {
                log.error("카카오 토큰 갱신 실패: 응답이 비었거나 액세스 토큰이 없습니다. 응답: {}", response);
                throw new RuntimeException("카카오 토큰 갱신에 실패했습니다");
            }

            log.info("카카오 액세스 토큰 갱신 성공");
            log.debug("AccessToken 존재 여부: {}", response.accessToken != null);
            log.debug("RefreshToken 존재 여부: {}", response.refreshToken != null);
            
            return response.accessToken;

        } catch (Exception e) {
            log.error("카카오 액세스 토큰 갱신 실패: {}", e.getMessage(), e);
            throw new RuntimeException("카카오 액세스 토큰 갱신에 실패했습니다", e);
        }
    }

    /**
     * 토큰 갱신 요청 파라미터 구성
     */
    private String buildRefreshTokenRequest(String refreshToken) {
        log.debug("토큰 갱신 요청 파라미터 구성 시작");
        
        String requestBody = String.format(
                "grant_type=refresh_token&client_id=%s&client_secret=%s&refresh_token=%s",
                clientId, clientSecret, refreshToken
        );
        
        log.debug("요청 파라미터 구성 완료 (토큰 정보는 보안상 마스킹)");
        return requestBody;
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
