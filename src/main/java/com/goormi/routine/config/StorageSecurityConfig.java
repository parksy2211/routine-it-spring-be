package com.goormi.routine.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class StorageSecurityConfig {

    /**
     * 이 체인은 '/api/storage/**' 만 처리하고,
     * 여기서는 OAuth2/JWT 필터를 전혀 쓰지 않습니다.
     * -> 비로그인/무토큰으로도 통과 (permitAll)
     */
    @Order(0) // ⬅️ 반드시 기존 체인보다 우선순위를 높게!
    @Bean
    public SecurityFilterChain storageChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher("/api/storage/**")
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                // 주의: 여기에는 .oauth2Login() 도, JWT 필터 추가도 절대 넣지 마세요
                .build();
    }
}
