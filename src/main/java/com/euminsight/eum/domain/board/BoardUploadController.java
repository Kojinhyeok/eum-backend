package com.euminsight.eum.domain.board;

import com.euminsight.eum.common.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.Set;

/**
 * 관리자 게시판 업로드 (에디터 이미지 / 첨부파일) → S3
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/upload")
@RequiredArgsConstructor
public class BoardUploadController {

    private static final long IMAGE_MAX = 10L * 1024 * 1024;   // 10MB
    private static final long FILE_MAX = 20L * 1024 * 1024;    // 20MB

    private static final Set<String> IMAGE_EXT = Set.of(".jpg", ".jpeg", ".png", ".gif", ".webp");
    private static final Set<String> FILE_EXT = Set.of(
            ".pdf", ".hwp", ".hwpx", ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx",
            ".txt", ".csv", ".zip", ".jpg", ".jpeg", ".png", ".gif", ".webp");

    private final S3Service s3Service;

    @PostMapping("/image")
    public ResponseEntity<Map<String, Object>> image(@RequestParam("file") MultipartFile file) {
        String ext = S3Service.extractExtension(file.getOriginalFilename());
        if (file.isEmpty()) return bad("빈 파일입니다.");
        if (!IMAGE_EXT.contains(ext)) return bad("이미지는 JPG, PNG, GIF, WEBP만 올릴 수 있습니다.");
        if (file.getSize() > IMAGE_MAX) return bad("이미지는 10MB 이하만 올릴 수 있습니다.");
        try {
            S3Service.UploadResult r = s3Service.uploadInline(file, "board/images");
            return ResponseEntity.ok(Map.of("url", r.url(), "fileKey", r.fileKey()));
        } catch (Exception e) {
            log.error("이미지 업로드 실패", e);
            return fail();
        }
    }

    @PostMapping("/file")
    public ResponseEntity<Map<String, Object>> file(@RequestParam("file") MultipartFile file) {
        String ext = S3Service.extractExtension(file.getOriginalFilename());
        if (file.isEmpty()) return bad("빈 파일입니다.");
        if (!FILE_EXT.contains(ext)) {
            return bad("올릴 수 없는 형식입니다. (PDF, HWP, 워드, 엑셀, 파워포인트, ZIP, 이미지 등)");
        }
        if (file.getSize() > FILE_MAX) return bad("첨부파일은 20MB 이하만 올릴 수 있습니다.");
        try {
            S3Service.UploadResult r = s3Service.uploadAttachment(file, "board/files");
            String name = file.getOriginalFilename() == null ? "file" + ext : file.getOriginalFilename();
            return ResponseEntity.ok(Map.of(
                    "name", name, "url", r.url(), "fileKey", r.fileKey(), "size", file.getSize()));
        } catch (Exception e) {
            log.error("첨부파일 업로드 실패", e);
            return fail();
        }
    }

    private static ResponseEntity<Map<String, Object>> bad(String msg) {
        return ResponseEntity.badRequest().body(Map.of("message", msg));
    }

    private static ResponseEntity<Map<String, Object>> fail() {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "업로드에 실패했습니다. 잠시 후 다시 시도해 주세요. (S3 설정 확인 필요)"));
    }
}