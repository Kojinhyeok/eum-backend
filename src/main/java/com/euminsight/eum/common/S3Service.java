package com.euminsight.eum.common;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    private static final String CACHE_CONTROL = "public, max-age=31536000, immutable"; // 파일명이 UUID라 변하지 않음

    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Value("${aws.s3.region}")
    private String region;

    @Value("${aws.s3.public-base-url:}")
    private String publicBaseUrl;

    public record UploadResult(String fileKey, String url) {}

    /** 에디터 이미지: 브라우저에서 바로 표시 */
    public UploadResult uploadInline(MultipartFile file, String folder) throws IOException {
        return put(file, folder, null);
    }

    /** 첨부파일: 원래 파일명(한글 포함)으로 다운로드되도록 Content-Disposition 지정 */
    public UploadResult uploadAttachment(MultipartFile file, String folder) throws IOException {
        String name = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
        String encoded = URLEncoder.encode(name, StandardCharsets.UTF_8).replace("+", "%20");
        return put(file, folder, "attachment; filename*=UTF-8''" + encoded);
    }

    private UploadResult put(MultipartFile file, String folder, String disposition) throws IOException {
        String fileKey = folder + "/" + LocalDate.now().toString().substring(0, 7) + "/"
                + UUID.randomUUID() + extractExtension(file.getOriginalFilename());

        PutObjectRequest.Builder req = PutObjectRequest.builder()
                .bucket(bucket)
                .key(fileKey)
                .contentType(file.getContentType() == null ? "application/octet-stream" : file.getContentType())
                .cacheControl(CACHE_CONTROL);
        if (disposition != null) req.contentDisposition(disposition);

        try (InputStream in = file.getInputStream()) {
            s3Client.putObject(req.build(), RequestBody.fromInputStream(in, file.getSize()));
        }
        return new UploadResult(fileKey, publicUrl(fileKey));
    }

    public String publicUrl(String fileKey) {
        String base = (publicBaseUrl == null || publicBaseUrl.isBlank())
                ? "https://" + bucket + ".s3." + region + ".amazonaws.com"
                : publicBaseUrl.replaceAll("/+$", "");
        return base + "/" + fileKey;
    }

    public static String extractExtension(String fileName) {
        if (fileName == null) return "";
        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) return "";
        String ext = fileName.substring(dot).toLowerCase();
        return ext.matches("\\.[a-z0-9]{1,8}") ? ext : "";
    }
}