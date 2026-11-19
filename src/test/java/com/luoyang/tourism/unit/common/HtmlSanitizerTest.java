package com.luoyang.tourism.unit.common;

import com.luoyang.tourism.common.util.HtmlSanitizer;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HtmlSanitizerTest {
    @Test
    void removesScriptAndEventHandlers() {
        String cleaned = HtmlSanitizer.clean("<p onclick=\"alert(1)\">洛阳</p><script>alert(1)</script><h2>龙门</h2>");
        assertThat(cleaned).contains("<p>洛阳</p>").contains("<h2>龙门</h2>")
                .doesNotContain("<script").doesNotContain("onclick");
    }
}
