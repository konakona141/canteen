package com.example.canteen.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.example.canteen.common.Result;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/canteen/common")
public class FileController {

    @Value("${spring.file.upload-path}")
    private String uploadPath;

    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) return Result.error("文件不能为空");

        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename == null ? "" : originalFilename.substring(originalFilename.lastIndexOf("."));
        String fileName = UUID.randomUUID() + suffix;
        File dest = new File(uploadPath + fileName);
        if (!dest.getParentFile().exists()) dest.getParentFile().mkdirs();

        try {
            file.transferTo(dest);
            // 返回给前端的访问路径：/uploads/uuid.jpg
            return Result.success("/uploads/" + fileName);
        } catch (IOException e) {
            return Result.error("上传失败：" + e.getMessage());
        }
    }
}