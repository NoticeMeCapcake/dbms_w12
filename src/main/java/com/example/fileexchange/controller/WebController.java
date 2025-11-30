package com.example.fileexchange.controller;

import com.example.fileexchange.model.FileInfo;
import com.example.fileexchange.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
@Slf4j
public class WebController {

    private final FileService fileService;

    @GetMapping
    public String index(Model model) {
        model.addAttribute("apiKey", "user-12345");
        return "index";
    }

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file,
                             @RequestParam("apiKey") String apiKey,
                             Model model) {
        try {
            fileService.uploadFile(file, apiKey);
            model.addAttribute("message", "File uploaded successfully!");
        } catch (IOException e) {
            log.error("Upload failed", e);
            model.addAttribute("error", "Upload failed: " + e.getMessage());
        }
        model.addAttribute("apiKey", apiKey);
        return "index";
    }

    @PostMapping("/list")
    public String listFiles(@RequestParam("apiKey") String apiKey, Model model) {
        List<FileInfo> files = fileService.listFiles(apiKey);
        model.addAttribute("files", files);
        model.addAttribute("apiKey", apiKey);
        return "index";
    }

    @PostMapping("/delete")
    public String deleteFile(@RequestParam("fileId") String fileId,
                             @RequestParam("apiKey") String apiKey,
                             Model model) {
        try {
            fileService.deleteFile(fileId, apiKey);
            model.addAttribute("message", "File deleted successfully!");
        } catch (Exception e) {
            log.error("Deletion failed", e);
            model.addAttribute("error", "Deletion failed: " + e.getMessage());
        }
        model.addAttribute("apiKey", apiKey);
        return "index";
    }

    @PostMapping("/share")
    public String generateShareLink(@RequestParam("fileId") String fileId,
                                    @RequestParam("apiKey") String apiKey,
                                    @RequestParam(defaultValue = "3600") int expiresIn,
                                    Model model) {
        try {
            String url = fileService.generatePresignedUrl(fileId, apiKey, expiresIn);
            model.addAttribute("shareUrl", url);
        } catch (Exception e) {
            log.error("Share link generation failed", e);
            model.addAttribute("error", "Share link generation failed: " + e.getMessage());
        }
        model.addAttribute("apiKey", apiKey);
        return "index";
    }

    // Получение статистики
    @PostMapping("/stats")
    public String getStats(@RequestParam("apiKey") String apiKey, Model model) {
        FileService.Stats stats = fileService.getStats(apiKey);
        model.addAttribute("stats", stats);
        model.addAttribute("apiKey", apiKey);
        return "index";
    }
}