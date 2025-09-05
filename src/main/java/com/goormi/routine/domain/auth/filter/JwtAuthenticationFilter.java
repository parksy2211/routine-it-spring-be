package com.goormi.routine.domain.auth.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.goormi.routine.common.response.ApiResponse;
import com.goormi.routine.domain.auth.service.JwtTokenProvider;
import com.goormi.routine.domain.auth.service.TokenService;
import com.goormi.routine.domain.user.entity.User;
import com.goormi.routine.domain.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                   FilterChain filterChain) throws ServletException, IOException {
        
        // Public endpoints 체크
        String path = request.getRequestURI();
        if (isPublicEndpoint(path)) {
            filterChain.doFilter(request, response);
            return;
        }
        
        String token = resolveToken(request);
        
        // 토큰이 없는 경우 - 그냥 진행 (SecurityConfig의 EntryPoint가 처리)
        if (!StringUtils.hasText(token)) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // 토큰 유효성 상세 검증
        JwtTokenProvider.TokenValidationResult validationResult = 
            jwtTokenProvider.validateTokenWithDetails(token);
        
        if (validationResult == JwtTokenProvider.TokenValidationResult.EXPIRED) {
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, 
                "토큰이 만료되었습니다. 다시 로그인해주세요.");
            return;
        }
        
        if (validationResult != JwtTokenProvider.TokenValidationResult.VALID) {
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, 
                "유효하지 않은 토큰입니다.");
            return;
        }
        
        // 블랙리스트 체크
        if (tokenService.isBlacklisted(token)) {
            log.warn("Blacklisted token used: {}", token);
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, 
                "로그아웃된 토큰입니다. 다시 로그인해주세요.");
            return;
        }
        
        // 사용자 정보 설정
        Long userId = jwtTokenProvider.getUserId(token);
        userRepository.findById(userId).ifPresentOrElse(
            user -> {
                List<SimpleGrantedAuthority> authorities = List.of(
                    new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
                );
                
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(userId, null, authorities);
                    
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("Authenticated user: {} with ID: {}", user.getEmail(), userId);
            },
            () -> {
                log.warn("User not found for token: {}", userId);
                try {
                    sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, 
                        "사용자를 찾을 수 없습니다.");
                } catch (IOException e) {
                    log.error("Error sending response", e);
                }
            }
        );
        
        // 사용자가 없으면 여기서 리턴
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            return;
        }
        
        filterChain.doFilter(request, response);
    }
    
    private boolean isPublicEndpoint(String path) {
        return path.startsWith("/api/health") ||
               path.startsWith("/swagger-ui") ||
               path.startsWith("/v3/api-docs") ||
               path.startsWith("/api/auth/check-nickname") ||
               path.startsWith("/api/auth/refresh") ||
               path.startsWith("/ws") ||
               path.startsWith("/oauth2") ||
               path.startsWith("/login/oauth2");
    }
    
    private void sendErrorResponse(HttpServletResponse response, int status, String message) 
            throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        
        final ApiResponse<Void> errorResponse = ApiResponse.error(message);
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
    
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}