package com.euminsight.eum.domain.board;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 일반 게시판 글 (화장품 연구동향 / 전문가 칼럼 / 회사공지 / 행사 및 세미나)
 */
@Entity
@Table(name = "board_post")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class BoardPost {

    private static final DateTimeFormatter DATE_LABEL = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private BoardType board;

    @Column(nullable = false, length = 200)
    private String title;

    /** 본문 (텍스트, 줄바꿈 유지) */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    /** 관련 링크 (선택: 원문, 신청 페이지 등) */
    @Column(length = 500)
    private String linkUrl;

    /** 목록 상단 고정 */
    @Builder.Default
    @Column(nullable = false)
    private boolean pinned = false;

    @Builder.Default
    @Column(nullable = false)
    private boolean published = true;

    @Builder.Default
    @Column(nullable = false)
    private int viewCount = 0;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /* ===== 화면 표시용 ===== */

    public String getDateLabel() {
        return createdAt == null ? "" : createdAt.format(DATE_LABEL);
    }

    /** 목록용 요약 (줄바꿈 제거, 120자) */
    public String getExcerpt() {
        if (content == null) return "";
        String flat = content.replaceAll("\\s+", " ").trim();
        return flat.length() > 120 ? flat.substring(0, 120) + "…" : flat;
    }

    public boolean isHasLink() {
        return linkUrl != null && !linkUrl.isBlank();
    }
}