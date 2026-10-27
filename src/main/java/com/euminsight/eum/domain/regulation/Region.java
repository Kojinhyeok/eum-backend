package com.euminsight.eum.domain.regulation;

/**
 * 국가별 최신규정 게시글의 권역.
 * 화면 표시용 이름/약칭/근거 법령을 함께 관리한다.
 * (새 권역이 필요하면 여기에 한 줄 추가하면 관리자·화면에 자동 반영)
 */
public enum Region {

    US("미국", "USA", "US", "US FDA MoCRA & OTC Regulations"),
    EU("유럽·영국", "EU/UK", "EU", "EC No 1223/2009 & SCPN Framework"),
    CN("중국", "China", "CN", "NMPA 화장품 감독관리조례(CSAR)"),
    ASEAN("아세안", "ASEAN", "SEA", "ASEAN Cosmetic Directive & Halal Law"),
    JP("일본", "Japan", "JP", "MHLW / PMDA Regulations"),
    KR("한국", "Korea", "KR", "식약처 화장품법 (MFDS)"),
    GLOBAL("기타 국가", "Global", "GL", "Other Jurisdictions");

    private final String label;
    private final String en;
    private final String shortCode;
    private final String framework;

    Region(String label, String en, String shortCode, String framework) {
        this.label = label;
        this.en = en;
        this.shortCode = shortCode;
        this.framework = framework;
    }

    /** URL 파라미터·DB 저장값 (US, EU, ...) */
    public String getCode() {
        return name();
    }

    public String getLabel() {
        return label;
    }

    public String getEn() {
        return en;
    }

    public String getShortCode() {
        return shortCode;
    }

    public String getFramework() {
        return framework;
    }

    /** 잘못된 값이면 null */
    public static Region fromCode(String code) {
        if (code == null || code.isBlank()) return null;
        for (Region r : values()) {
            if (r.name().equalsIgnoreCase(code.trim())) return r;
        }
        return null;
    }
}
