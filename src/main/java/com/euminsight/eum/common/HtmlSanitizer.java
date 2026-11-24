package com.euminsight.eum.common;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Safelist;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * 관리자 에디터에서 넘어온 본문 HTML 정리 (스크립트·이벤트 속성 제거)
 * - clean()     : 게시판(Quill) 본문
 * - cleanRich() : 글로벌 규제동향(SunEditor) 본문. 표·글자색·정렬 허용
 */
public final class HtmlSanitizer {

    private static final Safelist SAFELIST = Safelist.relaxed()
            .addTags("s", "hr")
            // Quill 정렬/들여쓰기 클래스 (ql-align-center, ql-indent-1 …)
            .addAttributes("p", "class")
            .addAttributes("h1", "class").addAttributes("h2", "class").addAttributes("h3", "class")
            .addAttributes("li", "class").addAttributes("blockquote", "class")
            .addAttributes("pre", "class", "spellcheck")
            .addAttributes("a", "target")
            .addEnforcedAttribute("a", "rel", "noopener noreferrer")
            .preserveRelativeLinks(false);

    /** 표·글자색·이미지 배치까지 허용하는 목록 (style 값은 아래 ALLOWED_STYLES만 남김) */
    private static final String[] RICH_STYLE_TAGS = {
            "p", "div", "span", "strong", "b", "em", "i", "u", "s", "h2", "h3", "h4",
            "ul", "ol", "li", "blockquote", "table", "thead", "tbody", "tr", "th", "td",
            "colgroup", "col", "figure", "figcaption", "img"
    };

    private static final Set<String> ALLOWED_STYLES = Set.of(
            "color", "background-color", "text-align", "font-weight", "font-style",
            "text-decoration", "text-decoration-line", "font-size", "width", "height",
            "max-width", "margin-left", "padding-left", "vertical-align"
    );

    private static final Safelist RICH_SAFELIST = buildRichSafelist();

    private static Safelist buildRichSafelist() {
        Safelist sl = Safelist.relaxed()
                .addTags("s", "hr", "figure", "figcaption", "colgroup", "col", "sub", "sup")
                .addAttributes("a", "target")
                .addEnforcedAttribute("a", "rel", "noopener noreferrer")
                .addAttributes("td", "colspan", "rowspan")
                .addAttributes("th", "colspan", "rowspan")
                .addAttributes("img", "data-align", "data-size", "data-proportion", "data-percentage")
                .preserveRelativeLinks(false);
        for (String tag : RICH_STYLE_TAGS) sl.addAttributes(tag, "style", "class");
        return sl;
    }

    private static final Document.OutputSettings OUTPUT = new Document.OutputSettings().prettyPrint(false);

    private HtmlSanitizer() {}

    public static String clean(String html) {
        if (html == null) return null;
        return Jsoup.clean(html, "", SAFELIST, OUTPUT);
    }

    /** 글로벌 규제동향 본문: 표·글자색 허용, style 은 안전한 속성만 남김 */
    public static String cleanRich(String html) {
        if (html == null) return null;
        String cleaned = Jsoup.clean(html, "", RICH_SAFELIST, OUTPUT);
        Document doc = Jsoup.parseBodyFragment(cleaned);
        doc.outputSettings(OUTPUT);
        for (Element el : doc.select("[style]")) {
            String filtered = filterStyle(el.attr("style"));
            if (filtered.isEmpty()) el.removeAttr("style");
            else el.attr("style", filtered);
        }
        // 에디터 내부용 class 외에는 제거 (se-* , __se__*)
        for (Element el : doc.select("[class]")) {
            List<String> keep = new ArrayList<>();
            for (String c : el.classNames()) {
                if (c.startsWith("se-") || c.startsWith("__se__")) keep.add(c);
            }
            if (keep.isEmpty()) el.removeAttr("class");
            else el.attr("class", String.join(" ", keep));
        }
        return doc.body().html();
    }

    private static String filterStyle(String style) {
        StringBuilder sb = new StringBuilder();
        for (String decl : style.split(";")) {
            int idx = decl.indexOf(':');
            if (idx < 0) continue;
            String prop = decl.substring(0, idx).trim().toLowerCase(Locale.ROOT);
            String value = decl.substring(idx + 1).trim();
            if (!ALLOWED_STYLES.contains(prop) || value.isEmpty()) continue;
            String lower = value.toLowerCase(Locale.ROOT);
            if (lower.contains("url(") || lower.contains("expression") || lower.contains("javascript")
                    || value.contains("<") || value.contains(">") || value.contains("\\")) continue;
            if (sb.length() > 0) sb.append(' ');
            sb.append(prop).append(": ").append(value).append(';');
        }
        return sb.toString();
    }

    /** 태그를 뺀 순수 텍스트 */
    public static String text(String html) {
        if (html == null) return "";
        return Jsoup.parse(html).text();
    }

    /** 내용이 비었는지 (Quill 빈 값 "<p><br></p>" 포함). 이미지만 있어도 내용으로 인정 */
    public static boolean isBlank(String html) {
        if (html == null) return true;
        Document doc = Jsoup.parse(html);
        return doc.text().isBlank() && doc.select("img").isEmpty();
    }
}