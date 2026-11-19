package com.luoyang.tourism.unit;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class MigrationContractTest {
    @Test
    void migrationsContainThirtyOneTablesAndNoPhysicalForeignKeys() throws IOException {
        Path directory = Path.of("src/main/resources/db/migration");
        StringBuilder all = new StringBuilder();
        try (Stream<Path> files = Files.list(directory)) {
            files.filter(path -> path.getFileName().toString().endsWith(".sql"))
                    .sorted()
                    .forEach(path -> {
                        try {
                            all.append(Files.readString(path)).append('\n');
                        } catch (IOException exception) {
                            throw new RuntimeException(exception);
                        }
                    });
        }
        String sql = all.toString().toUpperCase(Locale.ROOT);
        long createTableCount = sql.lines().filter(line -> line.startsWith("CREATE TABLE ")).count();
        assertThat(createTableCount).isEqualTo(31);
        assertThat(sql).doesNotContain("FOREIGN KEY", "REFERENCES", "ON DELETE CASCADE");
    }
}
