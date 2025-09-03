package com.goormi.routine.domain.auth.service;

import com.goormi.routine.domain.user.entity.User;
import com.goormi.routine.domain.user.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenService tokenService;

    // ✨ 추가: 카카오 토큰 조회용
    private final OAuth2AuthorizedClientService authorizedClientService;

    // ✨ 추가: 카카오 토큰 저장소(Redis/DB)
    private final KakaoTokenStore kakaoTokenStore;

    @Value("${app.oauth2.redirect-uri:http://localhost:8080/auth/success}")
    private String redirectUri;

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        // --- 1) 카카오 사용자 정보 ---
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        Object idAttribute = oAuth2User.getAttribute("id");
        log.info("ID attribute type: {}, value: {}",
                idAttribute != null ? idAttribute.getClass().getName() : "null",
                idAttribute);

        String kakaoId;
        if (idAttribute instanceof Long)      kakaoId = String.valueOf(idAttribute);
        else if (idAttribute instanceof String) kakaoId = (String) idAttribute;
        else throw new IllegalStateException("카카오 ID 타입이 예상과 다릅니다: " +
                    (idAttribute != null ? idAttribute.getClass().getName() : "null"));

        User user = userRepository.findByKakaoId(kakaoId)
                .orElseThrow(() -> new IllegalStateException("사용자를 찾을 수 없습니다."));

        // --- 2) 카카오 Access Token 확보 & 저장(나에게 보내기 등 사용) ---
        if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
            OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                    oauthToken.getAuthorizedClientRegistrationId(), oauthToken.getName());
            if (client != null && client.getAccessToken() != null) {
                String kakaoAccessToken = client.getAccessToken().getTokenValue();
                Instant expiresAt = client.getAccessToken().getExpiresAt();
                Duration ttl = (expiresAt != null)
                        ? Duration.between(Instant.now(), expiresAt)
                        : Duration.ofHours(6); // 만료가 없으면 임시 6시간

                kakaoTokenStore.save(user.getId(), kakaoAccessToken, ttl);
                log.info("Saved Kakao access token for user {} (ttl: {})", user.getId(), ttl);
            } else {
                log.warn("OAuth2AuthorizedClient 또는 AccessToken을 찾지 못해 카카오 토큰을 저장하지 못했습니다.");
            }
        } else {
            log.warn("Authentication이 OAuth2AuthenticationToken이 아니어서 카카오 토큰을 저장하지 못했습니다. type={}",
                    authentication.getClass().getName());
        }

        // --- 3) 우리 서비스 JWT 발급 & RefreshToken 저장 ---
        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());
        tokenService.saveRefreshToken(user.getId(), refreshToken, jwtTokenProvider.getRefreshTokenExpiration());

        // --- 4) RefreshToken 쿠키 저장 ---
        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(false); // HTTPS면 true 권장
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge((int) Duration.ofDays(7).toSeconds());
        response.addCookie(refreshTokenCookie);

        // --- 5) 신규 사용자 여부 & 리다이렉트 ---
        boolean isNewUser = user.getCreatedAt().plusMinutes(1).isAfter(java.time.LocalDateTime.now());
        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("accessToken", accessToken)
                .queryParam("isNewUser", isNewUser)
                .queryParam("userId", user.getId())
                .encode()
                .build()
                .toUriString();

        log.info("OAuth2 login successful for user: {}, redirecting to: {}", user.getEmail(), targetUrl);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
