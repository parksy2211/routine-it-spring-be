package com.goormi.routine.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * WebClient 설정
 * 김영한 스타일: 외부 설정은 별도 Configuration으로 분리
 */
@Configuration
public class WebClientConfig {

    /**
     * 카카오 API 호출용 WebClient Bean 등록
     */
    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(1024 * 1024)) // 1MB
                .build();
    }
}
