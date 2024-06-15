package org.codefromheaven.dto;

import org.codefromheaven.service.SettingsService;

public enum InternalSetting implements BaseSetting {

    IMAGE_NAME;

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
        return ElementType.INTERNAL_SETTINGS;
    }

}
