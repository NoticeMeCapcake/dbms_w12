package com.example.fileexchange.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class BucketInitialization {

    private final S3Client s3Client;

    @Value("${app.s3.bucket-name}")
    private String bucketName;

    @EventListener(ApplicationReadyEvent.class)
    public void initializeBucket() {
        log.info("Checking if bucket '{}' exists.", bucketName);
        try {
            HeadBucketRequest headBucketRequest = HeadBucketRequest.builder()
                    .bucket(bucketName)
                    .build();
            s3Client.headBucket(headBucketRequest);
            log.info("Bucket '{}' already exists.", bucketName);
        } catch (NoSuchBucketException e) {
            log.info("Bucket '{}' does not exist. Attempting to create it.", bucketName);
            try {
                CreateBucketRequest createBucketRequest = CreateBucketRequest.builder()
                        .bucket(bucketName)
                        .build();
                s3Client.createBucket(createBucketRequest);
                log.info("Bucket '{}' created successfully.", bucketName);
            } catch (S3Exception ex) {
                log.error("Failed to create bucket '{}': {}", bucketName, ex.awsErrorDetails().errorMessage(), ex);
                throw new RuntimeException("Could not initialize S3 bucket", ex);
            }
        } catch (S3Exception e) {
            log.error("Failed to check bucket status: {}", e.awsErrorDetails().errorMessage(), e);
            throw new RuntimeException("Could not initialize S3 bucket", e);
        }
    }
}