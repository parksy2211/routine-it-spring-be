package com.goormi.routine.domain.storage;

import com.goormi.routine.domain.storage.model.StorageFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StorageFileRepository extends JpaRepository<StorageFile, Long> {
    Optional<StorageFile> findByS3Key(String s3Key);
}