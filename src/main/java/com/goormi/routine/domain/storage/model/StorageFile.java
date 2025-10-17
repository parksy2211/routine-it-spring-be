package com.goormi.routine.domain.storage.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "storage_files", indexes = {
        @Index(name = "idx_storage_files_key", columnList = "s3Key", unique = true),
        @Index(name = "idx_storage_files_user", columnList = "userId"),
        @Index(name = "idx_storage_files_category", columnList = "category")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StorageFile {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false) private Long userId;   // 소유자(업로더)
    @Column(nullable = false, length = 64) private String category; // profile, proof-shot, group-room 등

    @Column(nullable = false, unique = true, length = 512)
    private String s3Key;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Visibility visibility; // PUBLIC/PRIVATE

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private StorageStatus status;  // PENDING/UPLOADED/DELETED

    private String contentType;
    private Long contentLength;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime uploadedAt;
}
