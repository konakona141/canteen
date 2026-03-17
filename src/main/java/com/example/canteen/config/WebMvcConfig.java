package com.example.canteen.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${spring.file.upload-path}")
    private String uploadPath;

    @Value("${spring.file.access-path}")
    private String accessPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 动态映射：将 /uploads/** 映射到本地 D:/canteen_uploads/
        registry.addResourceHandler(accessPath)
                .addResourceLocations("file:" + uploadPath);
    }
}