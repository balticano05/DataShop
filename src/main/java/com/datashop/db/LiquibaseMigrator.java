package com.datashop.db;

import com.datashop.config.DatabaseSettings;
import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Slf4j
public class LiquibaseMigrator implements DatabaseMigrator {

    private static final String MASTER_CHANGELOG = "db/changelog/db.changelog-master.yaml";
    private static final String SEED_CHANGELOG = "db/changelog/db.changelog-seed.yaml";

    @Override
    public void migrate(DatabaseSettings settings, boolean seedEnabled) {
        String changelog = seedEnabled ? SEED_CHANGELOG : MASTER_CHANGELOG;
        try (Connection connection = DriverManager.getConnection(
                settings.getJdbcUrl(), settings.getUsername(), settings.getPassword())) {
            Liquibase liquibase = new Liquibase(
                    changelog,
                    new ClassLoaderResourceAccessor(),
                    new JdbcConnection(connection)
            );
            liquibase.update(new Contexts());
            log.info("Applied Liquibase migrations from {}", changelog);
        } catch (SQLException | LiquibaseException exception) {
            throw new IllegalStateException("Cannot apply database migrations", exception);
        }
    }
}
