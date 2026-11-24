package com.euminsight.eum.domain.menu;

import com.euminsight.eum.domain.board.BoardType;
import com.euminsight.eum.domain.regulation.Region;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 메뉴·게시판·국가 탭 이름과 표시 여부를 화면/관리자에 제공한다.
 * 화면(layout.html)에서는 ${@menuService.headerMenus()} 처럼 직접 호출한다.
 * 설정값은 메모리에 캐시하고, 관리자가 저장하면 캐시를 비운다.
 */
@Service("menuService")
@RequiredArgsConstructor
public class MenuService {

    private final MenuSettingRepository menuSettingRepository;

    private volatile Map<String, MenuSetting> cache;

    /* =========================================================
       화면용 모델
       ========================================================= */

    @Getter
    public static class MenuNode {
        private final String code;
        private final String labelKo;
        /** null이면 data-en 속성을 출력하지 않음 (구글 번역 사용) */
        private final String labelEn;
        private final String url;
        private final String tabKey;
        private final List<String> activeKeys;
        private final List<MenuNode> children;

        MenuNode(String code, String labelKo, String labelEn, String url, String tabKey,
                 List<String> activeKeys, List<MenuNode> children) {
            this.code = code;
            this.labelKo = labelKo;
            this.labelEn = labelEn;
            this.url = url;
            this.tabKey = tabKey;
            this.activeKeys = activeKeys;
            this.children = children;
        }

        public boolean isHasChildren() {
            return !children.isEmpty();
        }

        public boolean isActiveFor(String active) {
            return active != null && activeKeys.contains(active);
        }
    }

    @Getter
    public static class RegionItem {
        private final Region region;
        private final String labelKo;
        private final String labelEn;

        RegionItem(Region region, String labelKo, String labelEn) {
            this.region = region;
            this.labelKo = labelKo;
            this.labelEn = labelEn;
        }

        public String getCode() {
            return region.getCode();
        }

        public String getShortCode() {
            return region.getShortCode();
        }

        /** 기존 템플릿 호환 (r.label) */
        public String getLabel() {
            return labelKo;
        }
    }

    /* =========================================================
       시작 시 기본값 채우기 (MenuSeeder)
       ========================================================= */

    @Transactional
    public int seedMissing() {
        Set<String> existing = menuSettingRepository.findAll().stream()
                .map(MenuSetting::getCode).collect(Collectors.toSet());
        List<MenuSetting> missing = MenuCatalog.ALL.stream()
                .filter(d -> !existing.contains(d.code()))
                .map(d -> MenuSetting.builder()
                        .code(d.code()).labelKo(d.labelKo()).labelEn(d.labelEn())
                        .visible(d.visible()).sortOrder(d.sortOrder()).build())
                .toList();
        menuSettingRepository.saveAll(missing);
        cache = null;
        return missing.size();
    }

    /* =========================================================
       공개 화면
       ========================================================= */

    /** 상단 메뉴 (표시 항목만, 순서대로) */
    public List<MenuNode> headerMenus() {
        List<MenuNode> result = new ArrayList<>();
        for (MenuCatalog.Def top : sorted(d -> MenuCatalog.GROUP_MENU.equals(d.group()) && d.isTopLevel())) {
            if (!isVisible(top)) continue;
            List<MenuNode> children = childNodes(top.code());
            String url = top.url();
            if (url == null) {
                if (children.isEmpty()) continue; // 하위 메뉴가 모두 숨김이면 상위 메뉴도 숨김
                url = children.get(0).getUrl();
            }
            result.add(node(top, url, children));
        }
        return result;
    }

    /** 게시판 탭 (INSIGHTS / NEWS 하위) */
    public List<MenuNode> tabs(String parentCode) {
        MenuCatalog.Def parent = MenuCatalog.find(parentCode);
        if (parent == null || !isVisible(parent)) return List.of();
        return childNodes(parentCode);
    }

    /** 숨김 게시판은 주소로 들어와도 404 */
    public boolean isBoardVisible(BoardType board) {
        MenuCatalog.Def d = MenuCatalog.find(MenuCatalog.boardCode(board));
        if (d == null) return true;
        MenuCatalog.Def parent = MenuCatalog.find(d.parent());
        return isVisible(d) && (parent == null || isVisible(parent));
    }

    public boolean isRegulationsVisible() {
        MenuCatalog.Def d = MenuCatalog.find("BOARD_REGULATIONS");
        MenuCatalog.Def parent = MenuCatalog.find("INSIGHTS");
        return isVisible(d) && isVisible(parent);
    }

    public String labelKo(String code) {
        MenuCatalog.Def d = MenuCatalog.find(code);
        if (d == null) return code;
        MenuSetting s = settings().get(code);
        return s != null ? s.getLabelKo() : d.labelKo();
    }

    public String labelEn(String code) {
        MenuCatalog.Def d = MenuCatalog.find(code);
        if (d == null) return null;
        MenuSetting s = settings().get(code);
        return blankToNull(s != null ? s.getLabelEn() : d.labelEn());
    }

    public String boardLabel(BoardType board) {
        return labelKo(MenuCatalog.boardCode(board));
    }

    public String boardLabelEn(BoardType board) {
        return labelEn(MenuCatalog.boardCode(board));
    }

    public String boardGroupLabel(BoardType board) {
        MenuCatalog.Def d = MenuCatalog.find(MenuCatalog.boardCode(board));
        return d == null ? board.getGroupLabel() : labelKo(d.parent());
    }

    /** 글로벌 규제동향에 표시할 국가 (순서대로) */
    public List<RegionItem> visibleRegions() {
        List<RegionItem> result = new ArrayList<>();
        for (MenuCatalog.Def d : sorted(d -> MenuCatalog.GROUP_REGION.equals(d.group()))) {
            if (!isVisible(d)) continue;
            Region r = Region.fromCode(d.code().substring("REGION_".length()));
            if (r != null) result.add(regionItem(r));
        }
        return result;
    }

    public boolean isRegionVisible(Region region) {
        MenuCatalog.Def d = MenuCatalog.find(MenuCatalog.regionCode(region));
        return d == null || isVisible(d);
    }

    public RegionItem regionItem(Region region) {
        String code = MenuCatalog.regionCode(region);
        return new RegionItem(region, labelKo(code), labelEn(code));
    }

    public String regionLabel(Region region) {
        return region == null ? "" : labelKo(MenuCatalog.regionCode(region));
    }

    public String regionLabelEn(Region region) {
        return region == null ? null : labelEn(MenuCatalog.regionCode(region));
    }

    /* =========================================================
       관리자
       ========================================================= */

    public record AdminRow(String code, String group, String parent, String parentLabel,
                           String url, String defaultKo, String labelKo, String labelEn,
                           boolean visible, int sortOrder, boolean topLevel) {
    }

    public record UpdateItem(String code, String labelKo, String labelEn, Boolean visible, Integer sortOrder) {
    }

    public List<AdminRow> adminRows() {
        List<AdminRow> rows = new ArrayList<>();
        // 상단 메뉴: 상위 → 하위 순서로 묶어서
        for (MenuCatalog.Def top : sorted(d -> MenuCatalog.GROUP_MENU.equals(d.group()) && d.isTopLevel())) {
            rows.add(row(top));
            for (MenuCatalog.Def c : sorted(d -> top.code().equals(d.parent()))) rows.add(row(c));
        }
        for (MenuCatalog.Def r : sorted(d -> MenuCatalog.GROUP_REGION.equals(d.group()))) rows.add(row(r));
        return rows;
    }

    @Transactional
    public void update(List<UpdateItem> items) {
        if (items == null || items.isEmpty()) return;
        Map<String, MenuSetting> current = menuSettingRepository.findAll().stream()
                .collect(Collectors.toMap(MenuSetting::getCode, Function.identity()));
        for (UpdateItem it : items) {
            MenuCatalog.Def d = it.code() == null ? null : MenuCatalog.find(it.code());
            if (d == null) continue;
            String ko = it.labelKo() == null ? "" : it.labelKo().trim();
            if (ko.isEmpty()) throw bad("'" + d.labelKo() + "' 항목의 한글 이름을 입력해 주세요.");
            if (ko.length() > 50) throw bad("한글 이름은 50자 이내로 입력해 주세요. (" + ko + ")");
            String en = it.labelEn() == null ? null : it.labelEn().trim();
            if (en != null && en.length() > 80) throw bad("영문 이름은 80자 이내로 입력해 주세요. (" + en + ")");

            MenuSetting s = current.getOrDefault(d.code(), MenuSetting.builder().code(d.code()).build());
            s.setLabelKo(ko);
            s.setLabelEn(blankToNull(en));
            s.setVisible(it.visible() == null ? s.isVisible() : it.visible());
            s.setSortOrder(it.sortOrder() == null ? d.sortOrder() : it.sortOrder());
            menuSettingRepository.save(s);
        }
        cache = null;
    }

    /* =========================================================
       내부
       ========================================================= */

    private Map<String, MenuSetting> settings() {
        Map<String, MenuSetting> c = cache;
        if (c == null) {
            c = menuSettingRepository.findAll().stream()
                    .collect(Collectors.toUnmodifiableMap(MenuSetting::getCode, Function.identity()));
            cache = c;
        }
        return c;
    }

    private boolean isVisible(MenuCatalog.Def d) {
        if (d == null) return false;
        MenuSetting s = settings().get(d.code());
        return s != null ? s.isVisible() : d.visible();
    }

    private int order(MenuCatalog.Def d) {
        MenuSetting s = settings().get(d.code());
        return s != null ? s.getSortOrder() : d.sortOrder();
    }

    private List<MenuCatalog.Def> sorted(java.util.function.Predicate<MenuCatalog.Def> filter) {
        return MenuCatalog.ALL.stream()
                .filter(filter)
                .sorted(Comparator.comparingInt(this::order))
                .toList();
    }

    private List<MenuNode> childNodes(String parentCode) {
        return sorted(d -> parentCode.equals(d.parent())).stream()
                .filter(this::isVisible)
                .map(d -> node(d, d.url(), List.of()))
                .toList();
    }

    private MenuNode node(MenuCatalog.Def d, String url, List<MenuNode> children) {
        return new MenuNode(d.code(), labelKo(d.code()), labelEn(d.code()), url, d.tabKey(), d.activeKeys(), children);
    }

    private AdminRow row(MenuCatalog.Def d) {
        MenuSetting s = settings().get(d.code());
        MenuCatalog.Def parent = d.parent() == null ? null : MenuCatalog.find(d.parent());
        return new AdminRow(d.code(), d.group(), d.parent(), parent == null ? null : labelKo(parent.code()),
                d.url(), d.labelKo(),
                s != null ? s.getLabelKo() : d.labelKo(),
                s != null ? s.getLabelEn() : d.labelEn(),
                isVisible(d), order(d), d.isTopLevel());
    }

    private static String blankToNull(String v) {
        return v == null || v.isBlank() ? null : v.trim();
    }

    private static IllegalArgumentException bad(String msg) {
        return new IllegalArgumentException(msg);
    }
}