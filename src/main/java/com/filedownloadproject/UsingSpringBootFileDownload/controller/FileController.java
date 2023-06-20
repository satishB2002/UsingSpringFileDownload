package com.filedownloadproject.UsingSpringBootFileDownload.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.filedownloadproject.UsingSpringBootFileDownload.config.FileStorageProperties;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
@RequestMapping("/file")
public class FileController {

    private static final String UPLOAD_DIR = "uploads";

    @Autowired
    private FileStorageProperties fileStorageProperties;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
           
            String fileName = StringUtils.cleanPath(file.getOriginalFilename());

       
            createUploadDirectory();

       
            Path filePath = Paths.get(fileStorageProperties.getUploadDir()).resolve(fileName);
            Files.copy(file.getInputStream(), filePath);

            return ResponseEntity.ok("File uploaded successfully: " + fileName);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Failed to upload file.");
        }
    }

    @GetMapping("/download/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) {
        try {
            // Load file as Resource
            Path filePath = Paths.get(fileStorageProperties.getUploadDir()).resolve(fileName);
            Resource resource = new UrlResource(filePath.toUri());

        
            if (resource.exists()) {
                // Set the content type and attachment header
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"");

           
                return ResponseEntity.ok()
                        .headers(headers)
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.notFound().build();
        }
    }

    private void createUploadDirectory() throws IOException {
        Path uploadDir = Paths.get(fileStorageProperties.getUploadDir());
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }
    }
}
