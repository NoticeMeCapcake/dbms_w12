package com.example.fileexchange.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "files")
@Getter
@Setter
public class FileInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "s3_key", nullable = false, unique = true)
    private String s3Key;

    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    @Column(name = "size_bytes", nullable = false)
    private Long sizeBytes;

    @Column(name = "api_key", nullable = false, updatable = false)
    private String apiKey;

    @Column(name = "upload_date", nullable = false, updatable = false)
    private LocalDateTime uploadDate;

    @PrePersist
    protected void onCreate() {
        uploadDate = LocalDateTime.now();
    }
}