package com.example.fileexchange.repository;

import com.example.fileexchange.model.FileInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FileInfoRepository extends JpaRepository<FileInfo, Long> {
    List<FileInfo> findByApiKey(String apiKey);
    boolean existsByS3KeyAndApiKey(String s3Key, String apiKey);
    boolean existsByS3Key(String s3Key); // Для проверки существования при удалении
    void deleteByS3Key(String s3Key);
}