package org.openjfx.dto;

public enum ElementType {
    SETTING("settings.csv"),
    SERVICE_COMMAND("service_commands.csv"),
    UPDATE_DAP_FOR_TESTS_COMMAND("update_dap_for_test_commands.csv"),
    LINK("links.csv");

    private final String fileName;

    ElementType(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

}
