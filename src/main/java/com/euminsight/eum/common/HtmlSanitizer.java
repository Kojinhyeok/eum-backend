package com.euminsight.eum.common;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;

/**
 * 관리자 에디터(Quill)에서 넘어온 본문 HTML 정리 (스크립트·이벤트 속성 제거)
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

    private static final Document.OutputSettings OUTPUT = new Document.OutputSettings().prettyPrint(false);

    private HtmlSanitizer() {}

    public static String clean(String html) {
        if (html == null) return null;
        return Jsoup.clean(html, "", SAFELIST, OUTPUT);
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