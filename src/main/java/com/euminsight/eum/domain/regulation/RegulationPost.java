package com.euminsight.eum.domain.regulation;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

/**
 * 국가별 최신규정 게시글 (관리자 등록 → /regulations 화면 카드로 노출)
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
