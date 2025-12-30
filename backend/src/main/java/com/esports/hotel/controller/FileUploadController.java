package com.esports.hotel.controller;

import com.esports.hotel.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 文件上传控制器
 */
@Slf4j
@Tag(name = "文件上传", description = "图片文件上传接口")
@RestController
@RequestMapping("/upload")
public class FileUploadController {

    // 上传文件存储的基础路径（可在application.yml中配置）
    @Value("${file.upload.path:uploads/}")
    private String uploadPath;

    // 文件访问的URL前缀
    @Value("${file.upload.url-prefix:/files/}")
    private String urlPrefix;

    @Operation(summary = "上传任务凭证图片", description = "上传任务完成的截图凭证")
    @PostMapping("/task-proof")
    public Result<Map<String, String>> uploadTaskProof(
            @Parameter(description = "图片文件") @RequestParam("file") MultipartFile file) {
        
        if (file.isEmpty()) {
            return Result.fail(400, "文件不能为空");
        }

        // 验证文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return Result.fail(400, "只能上传图片文件");
        }

        // 验证文件大小（限制5MB）
        long maxSize = 5 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            return Result.fail(400, "文件大小不能超过5MB");
        }

        try {
            // 生成文件名：日期/UUID.扩展名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            // 按日期创建子目录
            String dateFolder = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            String fileName = UUID.randomUUID().toString() + extension;
            
            // 完整的存储路径：uploadPath/task-proofs/yyyy-MM-dd/uuid.ext
            String relativePath = "task-proofs/" + dateFolder + "/" + fileName;
            
            // 确保使用绝对路径
            File uploadDir = new File(uploadPath);
            if (!uploadDir.isAbsolute()) {
                // 如果是相对路径，使用当前工作目录
                uploadDir = new File(System.getProperty("user.dir"), uploadPath);
            }
            
            Path fullPath = Paths.get(uploadDir.getAbsolutePath(), relativePath);

            // 创建目录（确保目录存在）
            Files.createDirectories(fullPath.getParent());
            
            log.info("准备保存文件到: {}", fullPath.toAbsolutePath());

            // 保存文件
            file.transferTo(fullPath.toFile());

            // 构建访问URL（urlPrefix已经在配置中包含/api前缀）
            String fileUrl = urlPrefix + relativePath;

            log.info("文件上传成功: {}, 访问URL: {}", fullPath.toAbsolutePath(), fileUrl);

            Map<String, String> result = new HashMap<>();
            result.put("url", fileUrl);
            result.put("filename", fileName);

            return Result.success(result, "文件上传成功");

        } catch (IOException e) {
            log.error("文件上传失败", e);
            return Result.fail(500, "文件上传失败: " + e.getMessage());
        }
    }
}
