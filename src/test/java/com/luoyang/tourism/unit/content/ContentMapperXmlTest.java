package com.luoyang.tourism.unit.content;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * MyBatis XML 契约测试，防止 Java Mapper 已声明方法但 XML 漏写 SQL。
 */
class ContentMapperXmlTest {
    @Test
    void publishedCategoryCountStatementIsDeclared() throws Exception {
        Configuration configuration = new Configuration();
        String resource = "mapper/content/ContentMapper.xml";
        Path xml = Path.of("src/main/resources").resolve(resource);
        try (InputStream input = Files.newInputStream(xml)) {
            new XMLMapperBuilder(input, configuration, resource, configuration.getSqlFragments()).parse();
        }

        assertThat(configuration.hasStatement(
                "com.luoyang.tourism.module.content.mapper.ContentMapper.countPublishedSpotsInCategory"))
                .isTrue();
    }
}