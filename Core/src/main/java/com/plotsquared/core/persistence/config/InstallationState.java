package com.plotsquared.core.persistence.config;

enum InstallationState {
    FRESH_INSTALLATION("Fresh installation with no prior data"),
    UPGRADE_FROM_V7("Upgrade from version 7.x"),
    UPGRADE_FROM_V8("Upgrade from version 8.x"),
    NO_MIGRATION_NEEDED("No migration needed, already on latest version");

    private final String description;

    InstallationState(final String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
