package com.example.fileexchange.service;

import com.example.fileexchange.model.FileInfo;
import com.example.fileexchange.repository.FileInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final FileInfoRepository fileInfoRepository;

    @Value("${app.s3.bucket-name}")
    private String bucketName;

    public FileInfo uploadFile(MultipartFile file, String apiKey) throws IOException {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new IllegalArgumentException("Filename cannot be empty");
        }
        long size = file.getSize();
        String s3Key = generateS3Key(apiKey);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .metadata(Map.of("owner", apiKey,
                                "filename", originalFilename))
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));

        FileInfo fileInfo = new FileInfo();
        fileInfo.setS3Key(s3Key);
        fileInfo.setOriginalFilename(originalFilename);
        fileInfo.setSizeBytes(size);
        fileInfo.setApiKey(apiKey);

        return fileInfoRepository.save(fileInfo);
    }

    public List<FileInfo> listFiles(String apiKey) {
        return fileInfoRepository.findByApiKey(apiKey);
    }

    public FileInfo getFileById(String fileId, String apiKey) {
        String s3Key = generateS3KeyFromId(fileId, apiKey);
        return fileInfoRepository.findByApiKeyAndS3Key(apiKey, s3Key)
                .orElseThrow(() -> new RuntimeException("File not found or access denied"));
    }

    public String generatePresignedUrl(String fileId, String apiKey, int expiresInSeconds) {
        String s3Key = generateS3KeyFromId(fileId, apiKey);
        if (!fileInfoRepository.existsByS3KeyAndApiKey(s3Key, apiKey)) {
            throw new RuntimeException("File not found or access denied");
        }

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(expiresInSeconds))
                .getObjectRequest(getObjectRequest)
                .build();

        PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
        return presignedRequest.url().toString();
    }

    @Transactional
    public void deleteFile(String fileId, String apiKey) {
        String s3Key = generateS3KeyFromId(fileId, apiKey);
        if (!fileInfoRepository.existsByS3KeyAndApiKey(s3Key, apiKey)) {
            throw new RuntimeException("File not found or access denied");
        }

        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build());
        } catch (S3Exception e) {
            log.error("Error deleting file from S3: {}", e.awsErrorDetails().errorMessage());
        }

        fileInfoRepository.deleteByApiKeyAndS3Key(apiKey, s3Key);
    }

    public Stats getStats(String apiKey) {
        List<FileInfo> files = fileInfoRepository.findByApiKey(apiKey);
        long totalFiles = files.size();
        long totalSize = files.stream().mapToLong(FileInfo::getSizeBytes).sum();
        return new Stats(totalFiles, totalSize);
    }

    private String generateS3Key(String apiKey) {
        String uuid = UUID.randomUUID().toString();
        return String.format("files/%s/%s", apiKey, uuid);
    }

    private String generateS3KeyFromId(String fileId, String apiKey) {
        if (!fileId.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")) {
            throw new IllegalArgumentException("Invalid file ID format");
        }
        return String.format("files/%s/%s", apiKey, fileId);
    }

        public record Stats(long totalFiles, long totalSizeBytes) {

    }
}