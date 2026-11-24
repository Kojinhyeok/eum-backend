package com.euminsight.eum.domain.regulation;

import com.euminsight.eum.common.HtmlSanitizer;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

/**
 * 글로벌 규제동향 게시글 (관리자 등록 → /regulations 목록, /regulations/{id} 상세)
 *
 * 2026-11 개편: 본문을 에디터 HTML(content) 하나로 통합.
 * 예전 칸(impact / checkpoints / keyPoint)은 되돌릴 수 있도록 DB에 그대로 남겨두고 화면에서는 쓰지 않는다.
 * (기존 글은 RegulationContentMigrator 가 서버 시작 시 content 로 한 번 옮김)
 */
@Entity
@Table(name = "regulation_post")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class RegulationPost {

    private static final DateTimeFormatter DATE_LABEL = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 권역 */
    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private Region region;

    /** 구분 배지 (예: 최신 개정/고시, 가이드라인 반영, 긴급 고시 대응) */
    @Column(length = 50, nullable = false)
    private String category;

    /** 업데이트 기준일 (정렬 기준) */
    @Column(nullable = false)
    private LocalDate noticeDate;

    /** 시행/적용 시점 메모 (예: 시행: 2024.12 ~ 상시) */
    @Column(length = 100)
    private String effectiveNote;

    @Column(nullable = false, length = 200)
    private String title;

    /** 개정 내용 요약 */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String summary;

    /** 규제 영향 분석 */
    @Column(columnDefinition = "TEXT")
    private String impact;

    /** 체크포인트 (한 줄에 하나씩) */
    @Column(columnDefinition = "TEXT")
    private String checkpoints;

    /** 상단 요약 카드에 노출되는 '규제 변경 포인트' 한 줄 */
    @Column(length = 300)
    private String keyPoint;

    /** 본문 (에디터 HTML, 저장 시 HtmlSanitizer.cleanRich 로 정리) */
    @Column(columnDefinition = "LONGTEXT")
    private String content;

    /** 목록 대표 이미지 (비우면 국가 코드 박스) */
    @Column(length = 500)
    private String thumbnailUrl;

    /** 원문 & 분석 리포트 링크 */
    @Column(length = 500)
    private String sourceUrl;

    @Builder.Default
    @Column(nullable = false)
    private boolean published = true;

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

    /* ===== 화면 표시용 (템플릿에서 ${post.dateLabel} 등으로 사용) ===== */

    public String getDateLabel() {
        return noticeDate == null ? "" : noticeDate.format(DATE_LABEL);
    }

    /** 상세 주소 */
    public String getPath() {
        return "/regulations/" + id;
    }

    public String getContentHtml() {
        return content == null ? "" : content;
    }

    public boolean isHasContent() {
        return !HtmlSanitizer.isBlank(content);
    }

    /** 목록 요약: 입력한 요약, 없으면 본문 앞부분 */
    public String getExcerpt() {
        String base = (summary != null && !summary.isBlank()) ? summary : HtmlSanitizer.text(content);
        String flat = base == null ? "" : base.replaceAll("\\s+", " ").trim();
        return flat.length() > 160 ? flat.substring(0, 160) + "…" : flat;
    }

    public boolean isHasThumbnail() {
        return thumbnailUrl != null && !thumbnailUrl.isBlank();
    }

    public boolean isHasCategory() {
        return category != null && !category.isBlank();
    }

    public List<String> getCheckpointList() {
        if (checkpoints == null || checkpoints.isBlank()) return List.of();
        return Arrays.stream(checkpoints.split("\\r?\\n"))
                .map(String::trim)
                .map(s -> s.replaceFirst("^[-•·*]\\s*", ""))
                .filter(s -> !s.isEmpty())
                .toList();
    }

    public boolean isHasImpact() {
        return impact != null && !impact.isBlank();
    }

    public boolean isHasCheckpoints() {
        return !getCheckpointList().isEmpty();
    }

    public boolean isHasKeyPoint() {
        return keyPoint != null && !keyPoint.isBlank();
    }

    public boolean isHasEffectiveNote() {
        return effectiveNote != null && !effectiveNote.isBlank();
    }

    public boolean isHasSource() {
        return sourceUrl != null && !sourceUrl.isBlank();
    }
}