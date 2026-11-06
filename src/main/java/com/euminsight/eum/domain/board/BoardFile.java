package com.euminsight.eum.domain.board;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

/**
 * 게시글 첨부파일 (S3에 올라간 파일 정보)
 */
@Embeddable
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class BoardFile {

    /** 원래 파일명 (화면 표시용) */
    @Column(name = "file_name", nullable = false, length = 255)
    private String name;

    @Column(name = "file_url", nullable = false, length = 700)
    private String url;

    @Column(name = "file_key", length = 500)
    private String fileKey;

    @Column(name = "file_size", nullable = false)
    private long size;

    /** 1.2 MB / 340 KB */
    public String getSizeLabel() {
        if (size >= 1024 * 1024) return String.format("%.1f MB", size / 1024.0 / 1024.0);
        return Math.max(1, Math.round(size / 1024.0)) + " KB";
    }

    /** PDF / HWP / XLSX … */
    public String getExtLabel() {
        int dot = name == null ? -1 : name.lastIndexOf('.');
        return dot < 0 ? "FILE" : name.substring(dot + 1).toUpperCase();
    }
}