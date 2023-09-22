package org.openjfx.dto;

public enum ScriptType {
    SERVICE_SCRIPT(Setting.SERVICE_SCRIPT_PATH.getValue()),
    UPDATE_DAP_FOR_TESTS(Setting.UPDATE_TEST_DAP_SCRIPT_PATH.getValue()),
    OPEN_REMOTE_APP_SCRIPT(Setting.OPEN_REMOTE_APP_SCRIPT_PATH.getValue());

    private final String path;

    ScriptType(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
