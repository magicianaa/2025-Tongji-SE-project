package com.esports.hotel.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 文件访问控制器
 * 用于提供上传文件的访问接口
 */
@Slf4j
@RestController
@RequestMapping("/files")
public class FileAccessController {

    @Value("${file.upload.path:uploads/}")
    private String uploadPath;

    /**
     * 访问上传的文件
     * 支持路径：/api/files/task-proofs/yyyy-MM-dd/filename.ext
     */
    @GetMapping("/**")
    public ResponseEntity<Resource> getFile(HttpServletRequest request) {
        try {
            // 获取请求的完整路径
            String requestURI = request.getRequestURI();
            // 从完整路径中提取文件相对路径
            // 例如：/api/files/task-proofs/2025-12-27/abc.jpg -> task-proofs/2025-12-27/abc.jpg
            String requestPath;
            if (requestURI.contains("/files/")) {
                requestPath = requestURI.substring(requestURI.indexOf("/files/") + "/files/".length());
            } else {
                log.error("无效的文件请求路径: {}", requestURI);
                return ResponseEntity.notFound().build();
            }

            // 确保使用绝对路径
            File uploadDir = new File(uploadPath);
            if (!uploadDir.isAbsolute()) {
                uploadDir = new File(System.getProperty("user.dir"), uploadPath);
            }
            
            Path filePath = Paths.get(uploadDir.getAbsolutePath(), requestPath);
            File file = filePath.toFile();
            
            log.info("尝试访问文件: requestURI={}, filePath={}", requestURI, filePath.toAbsolutePath());

            if (!file.exists() || !file.isFile()) {
                log.warn("文件不存在: {}", filePath.toAbsolutePath());
                return ResponseEntity.notFound().build();
            }

            Resource resource = new FileSystemResource(file);

            // 根据文件扩展名设置Content-Type
            String contentType = getContentType(file.getName());

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getName() + "\"")
                    .body(resource);

        } catch (Exception e) {
            log.error("文件访问失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    private String getContentType(String filename) {
        String extension = "";
        if (filename.contains(".")) {
            extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        }

        return switch (extension) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "bmp" -> "image/bmp";
            case "webp" -> "image/webp";
            default -> "application/octet-stream";
        };
    }
}
