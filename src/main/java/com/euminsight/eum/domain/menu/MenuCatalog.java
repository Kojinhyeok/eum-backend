package com.euminsight.eum.domain.menu;

import com.euminsight.eum.domain.board.BoardType;
import com.euminsight.eum.domain.regulation.Region;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 사이트 메뉴 구조와 기본값.
 * - 여기 있는 항목은 서버 시작 시 menu_setting 테이블에 없으면 기본값으로 추가된다.
 * - 이미 저장된 값(관리자가 바꾼 이름·표시 여부·순서)은 덮어쓰지 않는다.
 * - 새 메뉴·게시판이 필요하면 여기에 한 줄 추가한다.
 */
public final class MenuCatalog {

    public static final String GROUP_MENU = "MENU";
    public static final String GROUP_REGION = "REGION";

    /**
     * @param code       고유 코드
     * @param group      MENU(상단 메뉴·게시판) / REGION(글로벌 규제동향 국가 탭)
     * @param parent     상위 메뉴 코드 (최상위면 null)
     * @param labelKo    기본 한글 이름
     * @param labelEn    기본 영문 이름 (null이면 구글 번역)
     * @param url        이동 주소 (상위 메뉴가 null이면 첫 번째 표시 하위 메뉴 주소)
     * @param visible    기본 표시 여부
     * @param sortOrder  기본 순서
     * @param activeKeys header(active) 값 중 이 메뉴를 활성화할 값
     * @param tabKey     게시판 탭 현재값 (insightTabs / noticeTabs 의 current)
     */
    public record Def(String code, String group, String parent, String labelKo, String labelEn,
                      String url, boolean visible, int sortOrder, List<String> activeKeys, String tabKey) {

        public boolean isTopLevel() {
            return parent == null;
        }
    }

    public static final List<Def> ALL;

    static {
        List<Def> l = new ArrayList<>();
        // ===== 상단 메뉴 =====
        l.add(top("ABOUT", "회사 소개", "About us", "/about", 10, "about"));

        l.add(top("REG", "규제대응", "Regulatory Affairs", "/global", 20, "global", "domestic"));
        l.add(sub("REG_GLOBAL", "REG", "해외 규제대응", "Global Registrations", "/global", true, 10, null));
        l.add(sub("REG_DOMESTIC", "REG", "국내 규제대응", "Korean Registrations", "/domestic", true, 20, null));

        l.add(top("CLINICAL", "과학적 검증", "Scientific Verification", "/clinical", 30, "clinical"));
        l.add(sub("CLIN_INVITRO", "CLINICAL", "in vitro 시험", null, "/clinical#invitro", true, 10, null));
        l.add(sub("CLIN_HUMAN", "CLINICAL", "인체적용시험", null, "/clinical#human", true, 20, null));
        l.add(sub("CLIN_AI", "CLINICAL", "AI 데이터 분석", null, "/clinical#ai", true, 30, null));

        l.add(top("INSIGHTS", "인사이트", "Insights", null, 40, "regulations", "insights"));
        l.add(sub("BOARD_REGULATIONS", "INSIGHTS", "글로벌 규제동향", null, "/regulations", true, 10, "REGULATIONS"));
        l.add(board(BoardType.RESEARCH, "INSIGHTS", "화장품 연구동향", true, 20));
        l.add(board(BoardType.COLUMN, "INSIGHTS", "전문가 칼럼", false, 30));

        l.add(top("NEWS", "새소식", "News", null, 50, "notice"));
        l.add(board(BoardType.NOTICE, "NEWS", "알림", true, 10));
        l.add(board(BoardType.EVENT, "NEWS", "함께하는 기관", false, 20));
        l.add(board(BoardType.CLIENT, "NEWS", "주요 고객사", false, 30));

        // ===== 글로벌 규제동향 국가 탭 =====
        l.add(region(Region.US, "미국", "USA", true, 10));
        l.add(region(Region.EU, "EU/UK", "EU/UK", true, 20));
        l.add(region(Region.CN, "중국", "China", false, 30));
        l.add(region(Region.ASEAN, "아세안", "ASEAN", true, 40));
        l.add(region(Region.JP, "일본", "Japan", false, 50));
        l.add(region(Region.KR, "한국", "Korea", true, 60));
        l.add(region(Region.GLOBAL, "기타 국가", "Other countries", false, 70));
        ALL = Collections.unmodifiableList(l);
    }

    private MenuCatalog() {
    }

    public static String boardCode(BoardType t) {
        return "BOARD_" + t.name();
    }

    public static String regionCode(Region r) {
        return "REGION_" + r.name();
    }

    public static Def find(String code) {
        for (Def d : ALL) {
            if (d.code().equals(code)) return d;
        }
        return null;
    }

    private static Def top(String code, String ko, String en, String url, int order, String... activeKeys) {
        return new Def(code, GROUP_MENU, null, ko, en, url, true, order, List.of(activeKeys), null);
    }

    private static Def sub(String code, String parent, String ko, String en, String url, boolean visible, int order, String tabKey) {
        return new Def(code, GROUP_MENU, parent, ko, en, url, visible, order, List.of(), tabKey);
    }

    private static Def board(BoardType t, String parent, String ko, boolean visible, int order) {
        return new Def(boardCode(t), GROUP_MENU, parent, ko, null, t.getPath(), visible, order, List.of(), t.name());
    }

    private static Def region(Region r, String ko, String en, boolean visible, int order) {
        return new Def(regionCode(r), GROUP_REGION, null, ko, en, "/regulations?region=" + r.name(), visible, order, List.of(), null);
    }
}