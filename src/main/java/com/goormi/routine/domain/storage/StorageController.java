// src/main/java/com/goormi/routine/domain/storage/StorageController.java
package com.goormi.routine.domain.storage;

import com.goormi.routine.domain.storage.model.Visibility;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
@Tag(name = "Storage", description = "S3 Presigned URL 발급 및 조회 API")
public class StorageController {

    private final S3Presigner presigner;
    private final StorageFileService storageFileService; // ★ 추가: DB 메타/권한용

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
    @Operation(
            summary = "프로필 이미지 업로드 presign 발급",
            description = "사용자 ID와 파일명을 받아 S3 업로드용 presigned PUT URL을 반환합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(value = "{ \"uploadUrl\": \"https://...\", \"key\": \"users/1/profile/uuid.jpg\", \"expiresIn\": \"300\" }")
                            )
                    ),
                    @ApiResponse(responseCode = "400", description = "허용되지 않은 확장자/타입")
            }
    )
    // 1) 프로필 presign
    @PostMapping("/profile/presign")
    public ResponseEntity<Map<String, String>> presignProfilePut(
            @AuthenticationPrincipal(expression = "id") Long currentUserId,
            @RequestParam String filename,
            @RequestParam(defaultValue = "image/jpeg") String contentType,
            @RequestParam(defaultValue = "PRIVATE") Visibility visibility
    ) {
        String ext = safeExt(filename);
        if (!EXTS.contains(ext)) return ResponseEntity.badRequest().body(Map.of("error","ext must be jpg/jpeg/png/webp/heic/heif"));

        String key = "users/%d/profile/%s.%s".formatted(currentUserId, UUID.randomUUID(), ext);
        storageFileService.createPending(currentUserId, "profile", key, visibility, contentType);
        return makePutUrl(key, contentType);
    }

    // ---------- 2) 그룹루틴 인증샷(proof-shot) ----------
    @Operation(
            summary = "그룹루틴 인증샷 presign 발급",
            description = "그룹 ID, 사용자 ID를 받아 그룹루틴 인증샷 업로드용 presigned PUT URL을 반환합니다."
    )
    @PostMapping("/proof-shot/presign")
    public ResponseEntity<Map<String, String>> presignProofShotPut(
            @AuthenticationPrincipal(expression = "id") Long currentUserId,
            @RequestParam Long groupId,
            @RequestParam(defaultValue = "image/jpeg") String contentType,
            @RequestParam(defaultValue = "photo.jpg") String filename,
            @RequestParam(defaultValue = "PRIVATE") Visibility visibility
    ) {
        String ext = safeExt(filename);
        if (!EXTS.contains(ext)) return ResponseEntity.badRequest().body(Map.of("error","ext must be jpg/jpeg/png/webp/heic/heif"));

        // (권장) storageFileService.checkGroupMember(currentUserId, groupId);
        LocalDate d = LocalDate.now();
        String key = "proof-shots/%d/%d/%d/%02d/%02d/%s.%s"
                .formatted(groupId, currentUserId, d.getYear(), d.getMonthValue(), d.getDayOfMonth(), UUID.randomUUID(), ext);

        storageFileService.createPending(currentUserId, "proof-shot", key, visibility, contentType);
        return makePutUrl(key, contentType);
    }

    // ---------- 3) 그룹 채팅방 사진(group-room) ----------
    @Operation(
            summary = "그룹 채팅방 사진 presign 발급",
            description = "채팅방 ID, 사용자 ID를 받아 그룹 채팅방 사진 업로드용 presigned PUT URL을 반환합니다."
    )
    @PostMapping("/group-room/presign")
    public ResponseEntity<Map<String, String>> presignGroupRoomPut(
            @AuthenticationPrincipal(expression = "id") Long currentUserId,
            @RequestParam Long roomId,
            @RequestParam(defaultValue = "image/jpeg") String contentType,
            @RequestParam(defaultValue = "photo.jpg") String filename,
            @RequestParam(defaultValue = "PRIVATE") Visibility visibility
    ) {
        String ext = safeExt(filename);
        if (!EXTS.contains(ext)) return ResponseEntity.badRequest().body(Map.of("error","ext must be jpg/jpeg/png/webp/heic/heif"));


        LocalDate d = LocalDate.now();
        String key = "group-rooms/%d/%d/%d/%02d/%02d/%s.%s"
                .formatted(roomId, currentUserId, d.getYear(), d.getMonthValue(), d.getDayOfMonth(), UUID.randomUUID(), ext);

        storageFileService.createPending(currentUserId, "group-room", key, visibility, contentType);
        return makePutUrl(key, contentType);
    }

    // ---------- 일시 조회용 GET 프리사인 ----------
    @Operation(
            summary = "파일 접근 presign 발급",
            description = "S3에 업로드된 객체 키를 받아 임시 접근용 presigned GET URL을 반환합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(value = "{ \"url\": \"https://...\" }")
                            )
                    )
            }
    )
    @GetMapping("/presign-get")
    public Map<String, String> presignGet(
            @RequestParam String key,
            @RequestParam(required = false) String as,
            @AuthenticationPrincipal(expression = "id") Long currentUserId // null 허용
    ) {
        boolean download = "download".equalsIgnoreCase(as);

        var meta = storageFileService.getAndCheckViewPermission(currentUserId, key);

        GetObjectRequest gor = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .responseContentDisposition(download ? "attachment" : "inline")
                .responseContentType(meta.getContentType() != null ? meta.getContentType() : "application/octet-stream")
                .build();

        var req = presigner.presignGetObject(b -> b
                .signatureDuration(Duration.ofMinutes(expMin))
                .getObjectRequest(gor));

        return Map.of("url", req.url().toString());
    }
}
