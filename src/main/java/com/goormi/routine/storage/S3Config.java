package com.goormi.routine.storage;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
@RequiredArgsConstructor
public class S3Config {
    private S3Presigner presigner;

    @Bean
    public S3Presigner s3Presigner() {
        if (presigner == null) {
            presigner = S3Presigner.builder()
                    .region(Region.AP_NORTHEAST_2)
                    .build();
        }
        return presigner;
    }

    @PreDestroy
    public void close() {
        if (presigner != null) presigner.close();
    }
}