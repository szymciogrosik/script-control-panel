package org.openjfx.dto;

import org.openjfx.service.SettingsService;

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
        return SettingsService.getVariable(this);
    }

    @Override
    public ElementType getElementType() {
        return ElementType.SETTINGS;
    }

}
