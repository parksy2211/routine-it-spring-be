package com.goormi.routine.config;

import com.goormi.routine.domain.auth.filter.JwtAuthenticationFilter;
import com.goormi.routine.domain.auth.handler.CustomAuthenticationEntryPoint;
import com.goormi.routine.domain.auth.service.CustomOAuth2UserService;
import com.goormi.routine.domain.auth.service.OAuth2SuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Profile("!ci")
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable()) // 로컬 테스트용
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // ⬇️ 스토리지 presign 엔드포인트는 로컬 테스트 위해 공개
                        .requestMatchers(HttpMethod.POST, "/api/storage/profile/presign").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/storage/profile/presign-get").permitAll()
                        // CORS 사전요청 허용(브라우저 OPTIONS)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 공개 엔드포인트
                        .requestMatchers(
                                "/api/health/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**",
                                "/login/oauth2/**",
                                "/oauth2/**",
                                "/api/auth/signup",
                                "/api/auth/check-nickname",
                                "/api/auth/refresh",
                                "/api/personal-routines/**",
                                "/ws/**"
                        ).permitAll()

                        // 나머지는 인증 필요
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .successHandler(oAuth2SuccessHandler)
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",
                "http://localhost:8080",
                "http://localhost:5173",
                "http://54.180.93.1:3000",
                "http://54.180.93.1:8080",
                "https://petstore.swagger.io"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
