package com.luoyang.tourism.module.file.service.impl;

import com.luoyang.tourism.common.exception.BizException;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileServiceImplSceneTest {

    @Test
    void acceptsAdminFrontendSceneCodes() {
        List<String> scenes = List.of(
                "ADMIN_UPLOAD", "SCENIC_SPOT", "SCENIC_CATEGORY", "TRAVEL_GUIDE",
                "FOOD", "FOOD_CATEGORY", "HOME_BANNER", "RICH_TEXT",
                "KNOWLEDGE", "ADMIN_AVATAR");

        assertThat(scenes).allSatisfy(scene ->
                assertThat(FileServiceImpl.normalizeScene(scene)).isEqualTo(scene));
    }

    @Test
    void keepsLegacySceneCodesCompatible() {
        List<String> scenes = List.of("SPOT", "GUIDE", "FOOD", "BANNER", "AVATAR", "KNOWLEDGE");

        assertThat(scenes).allSatisfy(scene ->
                assertThat(FileServiceImpl.normalizeScene(scene)).isEqualTo(scene));
    }

    @Test
    void normalizesCaseAndWhitespace() {
        assertThat(FileServiceImpl.normalizeScene(" scenic_spot ")).isEqualTo("SCENIC_SPOT");
    }

    @Test
    void rejectsUnknownScene() {
        assertThatThrownBy(() -> FileServiceImpl.normalizeScene("UNKNOWN_SCENE"))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("不支持的文件业务场景");
    }
}