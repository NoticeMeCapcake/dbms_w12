package com.example.fileexchange.repository;

import com.example.fileexchange.model.FileInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface FileInfoRepository extends JpaRepository<FileInfo, Long> {
    List<FileInfo> findByApiKey(String apiKey);
    Optional<FileInfo> findByApiKeyAndS3Key(String apiKey, String s3Key);
    boolean existsByS3KeyAndApiKey(String s3Key, String apiKey);
    void deleteByApiKeyAndS3Key(String apiKey, String s3Key);
}