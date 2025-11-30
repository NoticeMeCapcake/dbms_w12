package com.example.fileexchange.controller;

import com.example.fileexchange.model.FileInfo;
import com.example.fileexchange.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class FileController {

    private final FileService fileService;

    @PostMapping("/files/upload")
    public ResponseEntity<FileInfo> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestHeader("X-API-Key") String apiKey) throws IOException {
        log.info("Uploading file for API Key: {}", apiKey);
        FileInfo savedFileInfo = fileService.uploadFile(file, apiKey);
        return ResponseEntity.ok(savedFileInfo);
    }

    @GetMapping("/files")
    public ResponseEntity<List<FileInfo>> listFiles(@RequestHeader("X-API-Key") String apiKey) {
        log.info("Listing files for API Key: {}", apiKey);
        List<FileInfo> files = fileService.listFiles(apiKey);
        return ResponseEntity.ok(files);
    }

    @GetMapping("/files/{fileId}")
    public ResponseEntity<FileInfo> getFileInfo(
            @PathVariable String fileId,
            @RequestHeader("X-API-Key") String apiKey) {
        log.info("Getting file info for ID: {} and API Key: {}", fileId, apiKey);
        FileInfo fileInfo = fileService.getFileById(fileId, apiKey);
        return ResponseEntity.ok(fileInfo);
    }

    @PostMapping("/files/{fileId}/share")
    public ResponseEntity<Map<String, String>> generateShareLink(
            @PathVariable String fileId,
            @RequestHeader("X-API-Key") String apiKey,
            @RequestBody Map<String, Integer> requestBody) {
        int expiresIn = requestBody.getOrDefault("expires_in", 3600); // по умолчанию 1 час
        log.info("Generating share link for file ID: {} and API Key: {}, expires in: {}s", fileId, apiKey, expiresIn);
        String url = fileService.generatePresignedUrl(fileId, apiKey, expiresIn);
        return ResponseEntity.ok(Map.of("url", url));
    }

    @DeleteMapping("/files/{fileId}")
    public ResponseEntity<Void> deleteFile(
            @PathVariable String fileId,
            @RequestHeader("X-API-Key") String apiKey) {
        log.info("Deleting file ID: {} for API Key: {}", fileId, apiKey);
        fileService.deleteFile(fileId, apiKey);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats")
    public ResponseEntity<FileService.Stats> getStats(@RequestHeader("X-API-Key") String apiKey) {
        log.info("Getting stats for API Key: {}", apiKey);
        FileService.Stats stats = fileService.getStats(apiKey);
        return ResponseEntity.ok(stats);
    }
}