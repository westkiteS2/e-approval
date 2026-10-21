package com.example.approval.document.config;

import jakarta.servlet.MultipartConfigElement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 파일 업로드 용량 설정.
 * 스프링 기본값은 파일당 1MB라서 결재 첨부파일용으로 늘린다.
 * (이 Bean이 있으면 application.yml 의 spring.servlet.multipart.* 설정은 무시됨)
 */
@Configuration
public class DocumentUploadConfig {

    public static final long MAX_FILE_SIZE_MB = 10;
    public static final int MAX_FILE_COUNT = 5;

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        long mb = 1024L * 1024L;
        long maxFileSize = MAX_FILE_SIZE_MB * mb;
        long maxRequestSize = (MAX_FILE_SIZE_MB * MAX_FILE_COUNT + 1) * mb; // 파일 5개 + JSON
        return new MultipartConfigElement("", maxFileSize, maxRequestSize, 0);
    }
}
