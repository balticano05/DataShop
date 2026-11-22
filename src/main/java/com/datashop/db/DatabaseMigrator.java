package com.datashop.db;

import com.datashop.config.DatabaseSettings;

public interface DatabaseMigrator {

    void migrate(DatabaseSettings settings, boolean seedEnabled);
}
