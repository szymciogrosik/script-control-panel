package org.codefromheaven.dto;

import org.codefromheaven.service.settings.InternalSettingsService;

public enum Setting implements BaseSetting{
    BASH_PATH,
    SERVICE_SCRIPT_PATH,
    UPDATE_TEST_DAP_SCRIPT_PATH,
    OPEN_REMOTE_APP_SCRIPT_PATH,
    SKAT_VPN_PATH,
    MAX_WINDOW_HEIGHT,
    MAX_WINDOW_WIDTH;

    @Override
    public String getName() {
        return name();
    }

    @Override
    public String getValue() {
        return InternalSettingsService.getVariable(this);
    }

    @Override
    public FileType getElementType() {
        return FileType.SETTINGS;
    }

}
