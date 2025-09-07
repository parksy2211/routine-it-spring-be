package com.goormi.routine.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/storage")
public class ProfileImageController {
    private final S3Presigner presigner;

    @Value("${routineit.s3.profile-bucket}")
    private String bucket;

    private static final Set<String> TYPES = Set.of("image/jpeg","image/png","image/webp","image/heic");
    private static final Set<String> EXTS  = Set.of("jpg","jpeg","png","webp","heic");

    @PostMapping("/profile/presign")
    public ResponseEntity<Map<String,String>> presignUpload(
            @RequestParam Long userId,
            @RequestParam String filename,
            @RequestParam(defaultValue="image/jpeg") String contentType) {

        contentType = contentType.toLowerCase();
        if (!TYPES.contains(contentType)) return ResponseEntity.badRequest().body(Map.of("error","unsupported contentType"));
        String ext = filename.contains(".") ? filename.substring(filename.lastIndexOf('.')+1).toLowerCase() : "";
        if (!EXTS.contains(ext)) return ResponseEntity.badRequest().body(Map.of("error","ext must be jpg/jpeg/png/webp/heic"));

        String key = "users/%d/profile/%s.%s".formatted(userId, UUID.randomUUID(), ext);

        PutObjectRequest por = PutObjectRequest.builder()
                .bucket(bucket).key(key)
                .contentType(contentType)
                .cacheControl("no-cache")
                .build();

        PresignedPutObjectRequest pre = presigner.presignPutObject(b -> b
                .signatureDuration(Duration.ofMinutes(5))
                .putObjectRequest(por));

        return ResponseEntity.ok(Map.of("uploadUrl", pre.url().toString(), "key", key));
    }

    //조회용
    @GetMapping("/profile/presign-get")
    public Map<String, String> presignGet(@RequestParam String key) {
        GetObjectRequest gor = GetObjectRequest.builder().bucket(bucket).key(key).build();
        PresignedGetObjectRequest req = presigner.presignGetObject(b -> b
                .signatureDuration(Duration.ofMinutes(5))
                .getObjectRequest(gor));
        return Map.of("url", req.url().toString());
    }
}
