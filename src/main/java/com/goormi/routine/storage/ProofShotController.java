package com.goormi.routine.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/storage/proof-shot")
public class ProofShotController {

    private final S3Presigner presigner;

    @Value("${routineit.s3.profile-bucket}")
    private String bucket;

    @Value("${routineit.s3.presign-exp-minutes:5}")
    private long expMin;

    /** 업로드 presign (필수: groupId, userId만) */
    @PostMapping("/presign")
    public Map<String, String> presignUpload(
            @RequestParam Long groupId,
            @RequestParam Long userId
            // filename, contentType 등은 받지 않음 (완전 최소화)
    ) {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));

        // 확장자 없이 키 생성 (원하면 ".jpg" 등 붙여도 무방)
        String key = "proof-shots/%d/%04d/%02d/%02d/%d/%s"
                .formatted(groupId, now.getYear(), now.getMonthValue(), now.getDayOfMonth(),
                        userId, UUID.randomUUID());

        // Content-Type을 서명에 포함하지 않음 → 클라가 어떤 Content-Type이든 전송 가능
        PutObjectRequest put = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .cacheControl("no-cache")
                .build();

        PresignedPutObjectRequest pre = presigner.presignPutObject(b -> b
                .signatureDuration(Duration.ofMinutes(expMin))
                .putObjectRequest(put));

        return Map.of(
                "uploadUrl", pre.url().toString(),
                "key", key,
                "expiresIn", String.valueOf(expMin * 60)
        );
    }

    /** 조회 presign (필수: key) */
    @GetMapping("/presign-get")
    public Map<String, String> presignGet(@RequestParam String key) {
        GetObjectRequest get = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .responseContentDisposition("inline")
                .build();

        PresignedGetObjectRequest pre = presigner.presignGetObject(b -> b
                .signatureDuration(Duration.ofMinutes(expMin))
                .getObjectRequest(get));

        return Map.of("url", pre.url().toString());
    }
}
