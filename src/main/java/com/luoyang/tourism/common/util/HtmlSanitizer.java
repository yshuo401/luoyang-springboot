package com.luoyang.tourism.common.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;

/**
 * HtmlSanitizer 通用工具类。
 */
public final class HtmlSanitizer {
    private static final Safelist SAFELIST = Safelist.relaxed()
            .addTags("h1", "h2", "h3", "h4", "h5", "h6", "figure", "figcaption", "section",
                    "table", "thead", "tbody", "tr", "th", "td", "hr", "span")
            .addAttributes(":all", "class")
            .addAttributes("a", "target", "rel")
            .addProtocols("a", "href", "http", "https")
            .addProtocols("img", "src", "http", "https");

    private HtmlSanitizer() {
    }

    public static String clean(String html) {
        if (html == null || html.isBlank()) {
            return html;
        }
        Document.OutputSettings outputSettings = new Document.OutputSettings().prettyPrint(false);
        return Jsoup.clean(html, "", SAFELIST, outputSettings);
    }
}
