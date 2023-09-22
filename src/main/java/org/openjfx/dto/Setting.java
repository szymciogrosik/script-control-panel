package org.openjfx.dto;

import org.openjfx.service.SettingsService;

public enum Setting {
    BASH_PATH,
    SERVICE_SCRIPT_PATH,
    UPDATE_TEST_DAP_SCRIPT_PATH,
    OPEN_REMOTE_APP_SCRIPT_PATH;

    public String getValue() {
        return SettingsService.getVariable(this);
    }
}
