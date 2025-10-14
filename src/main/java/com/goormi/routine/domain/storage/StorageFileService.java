// src/main/java/com/goormi/routine/domain/storage/StorageFileService.java
package com.goormi.routine.domain.storage;

import com.goormi.routine.domain.storage.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class StorageFileService {
    private final StorageFileRepository repo;
    private final S3Client s3;

    @Transactional
    public void createPending(Long userId, String category, String s3Key,
                              Visibility visibility, String contentType) {
        StorageFile f = StorageFile.builder()
                .userId(userId)
                .category(category)
                .s3Key(s3Key)
                .visibility(visibility)
                .status(StorageStatus.PENDING)
                .contentType(contentType)
                .createdAt(LocalDateTime.now())
                .build();
        repo.save(f);
    }

    @Transactional
    public void confirmUploaded(String bucket, String s3Key) {
        StorageFile f = repo.findByS3Key(s3Key)
                .orElseThrow(() -> new IllegalArgumentException("file not found"));
        var head = s3.headObject(HeadObjectRequest.builder()
                .bucket(bucket)
                .key(s3Key)
                .build());
        f.setStatus(StorageStatus.UPLOADED);
        f.setUploadedAt(LocalDateTime.now());
        f.setContentLength(head.contentLength());
        // contentType도 필요 시 head.contentType()으로 보정 가능
    }

    @Transactional(readOnly = true)
    public StorageFile getAndCheckViewPermission(Long currentUserId, String s3Key) {
        StorageFile f = repo.findByS3Key(s3Key)
                .orElseThrow(() -> new IllegalArgumentException("file not found"));

        if (f.getStatus() == StorageStatus.DELETED) {
            throw new SecurityException("deleted");
        }
        // PUBLIC은 모두 허용, PRIVATE은 소유자만
        if (f.getVisibility() == Visibility.PRIVATE && !f.getUserId().equals(currentUserId)) {
            throw new SecurityException("forbidden");
        }
        return f;
    }
}
