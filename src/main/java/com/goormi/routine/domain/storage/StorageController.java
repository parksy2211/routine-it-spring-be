// src/main/java/com/goormi/routine/storage/StorageController.java
package com.goormi.routine.domain.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/storage")
@RequiredArgsConstructor
@Validated
public class StorageController {

    private final S3Presigner presigner;

    @Value("${routineit.s3.profile-bucket}")
    private String bucket;

    @Value("${routineit.s3.presign-exp-minutes:5}")
    private long expMin;

    private static final Set<String> TYPES = Set.of(
            "image/jpeg","image/png","image/webp","image/heic","image/heif"
    );
    private static final Set<String> EXTS  = Set.of(
            "jpg","jpeg","png","webp","heic","heif"
    );

    // ---------- 공통 유틸 ----------
    private ResponseEntity<Map<String,String>> makePutUrl(String key, String contentType) {
        if (!TYPES.contains(contentType.toLowerCase())) {
            return ResponseEntity.badRequest().body(Map.of("error","unsupported contentType"));
        }
        PutObjectRequest por = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .cacheControl("no-cache")
                .build();

        PresignedPutObjectRequest pre = presigner.presignPutObject(b -> b
                .signatureDuration(Duration.ofMinutes(expMin))
                .putObjectRequest(por));

        return ResponseEntity.ok(Map.of(
                "uploadUrl", pre.url().toString(),
                "key", key,
                "expiresIn", String.valueOf(expMin * 60)
        ));
    }

    private static String safeExt(String filename) {
        String ext = filename.lastIndexOf('.') > 0
                ? filename.substring(filename.lastIndexOf('.')+1).toLowerCase()
                : "";
        return ext;
    }

    // ---------- 1) 프로필 이미지 ----------
    @PostMapping("/profile/presign")
    public ResponseEntity<Map<String, String>> presignProfilePut(
            @RequestParam Long userId,
            @RequestParam String filename,
            @RequestParam(defaultValue = "image/jpeg") String contentType) {

        String ext = safeExt(filename);
        if (!EXTS.contains(ext)) {
            return ResponseEntity.badRequest().body(Map.of("error","ext must be jpg/jpeg/png/webp/heic/heif"));
        }
        String key = "users/%d/profile/%s.%s".formatted(userId, UUID.randomUUID(), ext);
        return makePutUrl(key, contentType);
    }

    // ---------- 2) 그룹루틴 인증샷(proof-shot) ----------
    @PostMapping("/proof-shot/presign")
    public ResponseEntity<Map<String, String>> presignProofShotPut(
            @RequestParam Long groupId,
            @RequestParam Long userId,
            @RequestParam(defaultValue = "image/jpeg") String contentType,
            @RequestParam(defaultValue = "photo.jpg") String filename) {

        String ext = safeExt(filename);
        if (!EXTS.contains(ext)) {
            return ResponseEntity.badRequest().body(Map.of("error","ext must be jpg/jpeg/png/webp/heic/heif"));
        }
        LocalDate d = LocalDate.now();
        String key = "proof-shots/%d/%d/%d/%02d/%02d/%s.%s"
                .formatted(groupId, userId, d.getYear(), d.getMonthValue(), d.getDayOfMonth(), UUID.randomUUID(), ext);
        return makePutUrl(key, contentType);
    }

    // ---------- 3) 그룹 채팅방 사진(group-room) ----------
    @PostMapping("/group-room/presign")
    public ResponseEntity<Map<String, String>> presignGroupRoomPut(
            @RequestParam Long roomId,
            @RequestParam Long userId,
            @RequestParam(defaultValue = "image/jpeg") String contentType,
            @RequestParam(defaultValue = "photo.jpg") String filename) {

        String ext = safeExt(filename);
        if (!EXTS.contains(ext)) {
            return ResponseEntity.badRequest().body(Map.of("error","ext must be jpg/jpeg/png/webp/heic/heif"));
        }
        LocalDate d = LocalDate.now();
        String key = "group-rooms/%d/%d/%02d/%02d/%s.%s"
                .formatted(roomId, d.getYear(), d.getMonthValue(), d.getDayOfMonth(), UUID.randomUUID(), ext);
        return makePutUrl(key, contentType);
    }

    // ---------- (공통) 일시 조회용 GET 프리사인 ----------
    @GetMapping("/presign-get")
    public Map<String, String> presignGet(@RequestParam String key,
                                          @RequestParam(required = false) String as) {
        boolean download = "download".equalsIgnoreCase(as);

        // 람다 밖에서 요청 객체를 완성
        GetObjectRequest gor = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .responseContentDisposition(download ? "attachment" : "inline")
                .build();

        PresignedGetObjectRequest req = presigner.presignGetObject(b -> b
                .signatureDuration(Duration.ofMinutes(expMin))
                .getObjectRequest(gor));

        return Map.of("url", req.url().toString());
    }
}
