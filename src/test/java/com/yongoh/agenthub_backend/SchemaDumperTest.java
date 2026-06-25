package com.yongoh.agenthub_backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
    "spring.jpa.properties.javax.persistence.schema-generation.scripts.action=create",
    "spring.jpa.properties.javax.persistence.schema-generation.scripts.create-target=schema-dump.sql",
    "spring.jpa.hibernate.ddl-auto=none"
})
class SchemaDumperTest {
    @Test
    void dumpSchema() {}
}
