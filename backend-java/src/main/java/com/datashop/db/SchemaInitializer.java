package com.datashop.db;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class SchemaInitializer {

    private final Jpa jpa;

    public void initialize() {
        String script = read("db/schema.sql");
        List<String> statements = splitStatements(script);
        jpa.write(entityManager -> {
            for (String statement : statements) {
                entityManager.createNativeQuery(statement).executeUpdate();
            }
            migrateLegacyTypes(entityManager);
            ensureCheckConstraint(entityManager, "users", "chk_users_role",
                    "role IN ('admin', 'customer')");
            ensureCheckConstraint(entityManager, "orders", "chk_orders_status",
                    "status IN ('pending', 'paid', 'cancelled', 'refunded')");
            return null;
        });
        log.info("Database schema is ready ({} statements)", statements.size());
    }

    private void migrateLegacyTypes(EntityManager entityManager) {
        if ("USER-DEFINED".equals(columnDataType(entityManager, "users", "role"))) {
            entityManager.createNativeQuery(
                    "ALTER TABLE users ALTER COLUMN role DROP DEFAULT").executeUpdate();
            entityManager.createNativeQuery(
                    "ALTER TABLE users ALTER COLUMN role TYPE varchar(20) USING role::text")
                    .executeUpdate();
            entityManager.createNativeQuery(
                    "ALTER TABLE users ALTER COLUMN role SET DEFAULT 'customer'").executeUpdate();
            log.info("Migrated legacy users.role enum to varchar");
        }

        if ("USER-DEFINED".equals(columnDataType(entityManager, "orders", "status"))) {
            entityManager.createNativeQuery(
                    "ALTER TABLE orders ALTER COLUMN status DROP DEFAULT").executeUpdate();
            entityManager.createNativeQuery(
                    "ALTER TABLE orders ALTER COLUMN status TYPE varchar(20) USING status::text")
                    .executeUpdate();
            entityManager.createNativeQuery(
                    "ALTER TABLE orders ALTER COLUMN status SET DEFAULT 'pending'").executeUpdate();
            log.info("Migrated legacy orders.status enum to varchar");
        }

        if ("ARRAY".equals(columnDataType(entityManager, "products", "images"))) {
            entityManager.createNativeQuery(
                    "ALTER TABLE products ALTER COLUMN images DROP DEFAULT").executeUpdate();
            entityManager.createNativeQuery(
                    "ALTER TABLE products ALTER COLUMN images TYPE text USING to_json(images)::text")
                    .executeUpdate();
            entityManager.createNativeQuery(
                    "ALTER TABLE products ALTER COLUMN images SET DEFAULT '[]'").executeUpdate();
            log.info("Migrated legacy products.images array to JSON text");
        }
    }

    private String columnDataType(
            EntityManager entityManager,
            String table,
            String column
    ) {
        List<?> rows = entityManager.createNativeQuery("""
                        select data_type from information_schema.columns
                        where table_schema = current_schema()
                          and table_name = :table
                          and column_name = :column
                        """)
                .setParameter("table", table)
                .setParameter("column", column)
                .getResultList();
        return rows.isEmpty() ? null : String.valueOf(rows.getFirst());
    }

    private void ensureCheckConstraint(
            EntityManager entityManager,
            String table,
            String name,
            String expression
    ) {
        List<?> rows = entityManager.createNativeQuery("""
                        select 1 from information_schema.table_constraints
                        where table_schema = current_schema()
                          and table_name = :table
                          and constraint_name = :name
                        """)
                .setParameter("table", table)
                .setParameter("name", name)
                .getResultList();
        if (rows.isEmpty()) {
            entityManager.createNativeQuery(
                            "ALTER TABLE " + table + " ADD CONSTRAINT " + name
                                    + " CHECK (" + expression + ")")
                    .executeUpdate();
            log.info("Added missing constraint {} on {}", name, table);
        }
    }

    private String read(String resource) {
        try (InputStream stream = SchemaInitializer.class.getClassLoader().getResourceAsStream(resource)) {
            if (stream == null) {
                throw new IllegalStateException("Missing classpath resource: " + resource);
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot read " + resource, exception);
        }
    }

    private List<String> splitStatements(String script) {
        List<String> statements = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inLineComment = false;
        for (String line : script.split("\\R")) {
            String trimmed = line.trim();
            if (inLineComment) {
                if (trimmed.endsWith("*/")) {
                    inLineComment = false;
                }
                continue;
            }
            if (trimmed.startsWith("--") || trimmed.isEmpty()) {
                continue;
            }
            if (trimmed.startsWith("/*")) {
                if (!trimmed.endsWith("*/")) {
                    inLineComment = true;
                }
                continue;
            }
            current.append(line).append('\n');
            String assembled = current.toString();
            if (assembled.trim().endsWith(";")) {
                String statement = assembled.trim();
                statements.add(statement.substring(0, statement.length() - 1));
                current.setLength(0);
            }
        }
        String remaining = current.toString().trim();
        if (!remaining.isEmpty()) {
            statements.add(remaining);
        }
        return statements;
    }
}
