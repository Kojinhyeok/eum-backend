package com.euminsight.eum.domain.board;

/**
 * 게시판 종류.
 * - 화면에 보이는 이름·표시 여부는 관리자 > 메뉴·탭 관리(MenuService)에서 바뀐다. 여기 label은 기본값.
 * - group: URL 첫 경로 (/insights, /notice)
 * - slug : URL 두 번째 경로 (/insights/research ...)
 */
public enum BoardType {

    RESEARCH("insights", "research", "화장품 연구동향", "인사이트"),
    COLUMN("insights", "column", "전문가 칼럼", "인사이트"),
    NOTICE("notice", "company", "알림", "새소식"),
    EVENT("notice", "event", "함께하는 기관", "새소식"),
    CLIENT("notice", "clients", "주요 고객사", "새소식");

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

    /**
     * 구분 배지·대표 이미지·목록 요약·원문 링크를 쓰는 게시판 (썸네일 목록으로 표시)
     * - 화장품 연구동향, 전문가 칼럼
     */
    public boolean isRichList() {
        return this == RESEARCH || this == COLUMN;
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