package com.euminsight.eum.domain.board;

import com.euminsight.eum.common.HtmlSanitizer;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import org.springframework.web.util.HtmlUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 일반 게시판 글 (화장품 연구동향 / 전문가 칼럼 / 회사공지 / 행사 및 세미나)
 */
@Entity
@Table(name = "board_post")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class BoardPost {

    private static final DateTimeFormatter DATE_LABEL = DateTimeFormatter.ofPattern("yyyy.MM.dd");
    /** 에디터 도입 전(텍스트) 글인지 구분용 */
    private static final Pattern HTML_TAG = Pattern.compile("<(p|h[1-6]|ul|ol|li|img|blockquote|pre|br|div)[\\s>/]", Pattern.CASE_INSENSITIVE);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private BoardType board;

    @Column(nullable = false, length = 200)
    private String title;

    /** 본문 (에디터 HTML, 저장 시 정리됨 / 예전 글은 일반 텍스트) */
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

    /** 첨부파일 */
    @Builder.Default
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "board_post_file", joinColumns = @JoinColumn(name = "post_id"))
    @OrderColumn(name = "sort_order")
    @BatchSize(size = 20)
    private List<BoardFile> attachments = new ArrayList<>();

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

    /** 상세 화면 본문 HTML (예전 텍스트 글은 줄바꿈을 <br>로 변환) */
    public String getContentHtml() {
        if (content == null) return "";
        if (isHtmlContent()) return content;
        return HtmlUtils.htmlEscape(content).replace("\r\n", "\n").replace("\n", "<br>");
    }

    public boolean isHtmlContent() {
        return content != null && HTML_TAG.matcher(content).find();
    }

    /** 목록용 요약 (태그·줄바꿈 제거, 120자) */
    public String getExcerpt() {
        if (content == null) return "";
        String flat = (isHtmlContent() ? HtmlSanitizer.text(content) : content).replaceAll("\\s+", " ").trim();
        return flat.length() > 120 ? flat.substring(0, 120) + "…" : flat;
    }

    public boolean isHasAttachments() {
        return attachments != null && !attachments.isEmpty();
    }

    public boolean isHasLink() {
        return linkUrl != null && !linkUrl.isBlank();
    }
}