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
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/storage/group-room")
public class GroupImageController {

    private final S3Presigner presigner;

    @Value("${routineit.s3.profile-bucket}")
    private String bucket;

    @Value("${routineit.s3.presign-exp-minutes:5}")
    private long expMin;

    /** 업로드 presign (필수: roomId, userId만) */
    @PostMapping("/presign")
    public Map<String, String> presignUpload(
            @RequestParam Long roomId,
            @RequestParam Long userId
    ) {
        String key = "group-rooms/%d/images/%d/%s"
                .formatted(roomId, userId, UUID.randomUUID());

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
