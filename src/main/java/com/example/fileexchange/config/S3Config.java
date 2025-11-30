package com.example.fileexchange.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Configuration
public class S3Config {

    @Value("${app.s3.endpoint}")
    private String s3Endpoint;

    @Value("${app.s3.access-key}")
    private String accessKey;

    @Value("${app.s3.secret-key}")
    private String secretKey;

    @Value("${app.s3.region}")
    private String region;

    @PostConstruct
    public void setUp() {
        System.setProperty("aws.region", this.region);
    }

    @Bean
    public StaticCredentialsProvider credentialsProvider() {
        AwsBasicCredentials awsCreds = AwsBasicCredentials.create(accessKey, secretKey);
        return StaticCredentialsProvider.create(awsCreds);
    }

    @Bean
    public S3Client s3Client(StaticCredentialsProvider credentialsProvider) {
        AwsBasicCredentials awsCreds = AwsBasicCredentials.create(accessKey, secretKey);
        return S3Client.builder()
                .endpointOverride(URI.create(s3Endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .region(Region.EU_SOUTH_1)
                .forcePathStyle(true)
                .credentialsProvider(credentialsProvider)
                .build();
    }

    @Bean
    public S3Presigner s3Presigner(S3Client s3Client, StaticCredentialsProvider credentialsProvider) {
        return S3Presigner.builder()
                .s3Client(s3Client)
                .endpointOverride(URI.create(s3Endpoint))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .build())
                .credentialsProvider(credentialsProvider)
                .region(Region.EU_SOUTH_1)
                .build();
    }
}