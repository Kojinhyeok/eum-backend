package com.euminsight.eum.domain.board;

/**
 * 게시판 종류.
 * - group: URL 첫 경로 (/insights, /notice)
 * - slug : URL 두 번째 경로 (/insights/research ...)
 */
public enum BoardType {

    RESEARCH("insights", "research", "화장품 연구동향", "인사이트"),
    COLUMN("insights", "column", "전문가 칼럼", "인사이트"),
    NOTICE("notice", "company", "회사공지", "공지사항"),
    EVENT("notice", "event", "행사 및 세미나", "공지사항");

    private final String group;
    private final String slug;
    private final String label;
    private final String groupLabel;

    BoardType(String group, String slug, String label, String groupLabel) {
        this.group = group;
        this.slug = slug;
        this.label = label;
        this.groupLabel = groupLabel;
    }

    /** DB 저장값 (RESEARCH, COLUMN, ...) */
    public String getCode() {
        return name();
    }

    public String getGroup() {
        return group;
    }

    public String getSlug() {
        return slug;
    }

    public String getLabel() {
        return label;
    }

    public String getGroupLabel() {
        return groupLabel;
    }

    /** 목록 주소 (예: /insights/research) */
    public String getPath() {
        return "/" + group + "/" + slug;
    }

    /** /{group}/{slug} 로 게시판 찾기. 없으면 null */
    public static BoardType fromPath(String group, String slug) {
        for (BoardType t : values()) {
            if (t.group.equals(group) && t.slug.equals(slug)) return t;
        }
        return null;
    }

    public static BoardType fromCode(String code) {
        if (code == null || code.isBlank()) return null;
        for (BoardType t : values()) {
            if (t.name().equalsIgnoreCase(code.trim())) return t;
        }
        return null;
    }
}