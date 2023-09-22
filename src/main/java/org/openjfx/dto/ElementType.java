package org.openjfx.dto;

public enum ElementType {
    SETTING("settings.csv", "my_own_settings.csv"),
    SERVICE_COMMAND("service_commands.csv", "my_own_service_commands.csv"),
    UPDATE_DAP_FOR_TESTS_COMMAND("update_dap_for_test_commands.csv", "my_own_update_dap_for_test_commands.csv"),
    LINK("links.csv", "my_own_links.csv"),
    REMOTE_APP("open_remote_apps.csv", "my_own_open_remote_apps.csv");

    private final String defaultName;
    private final String personalizedConfigName;

    ElementType(String defaultName, String personalizedConfigName) {
        this.defaultName = defaultName;
        this.personalizedConfigName = personalizedConfigName;
    }

    public String getDefaultFileName() {
        return defaultName;
    }

    public String getPersonalizedConfigName() {
        return personalizedConfigName;
    }
}
