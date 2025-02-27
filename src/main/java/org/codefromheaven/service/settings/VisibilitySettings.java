package org.codefromheaven.service.settings;

import org.codefromheaven.dto.FileType;
import org.codefromheaven.dto.data.ButtonDTO;
import org.codefromheaven.dto.settings.KeyValueDTO;
import org.codefromheaven.dto.settings.SettingsDTO;
import org.codefromheaven.dto.settings.VisibilitySettingKey;

import static org.codefromheaven.service.settings.SettingsServiceBase.*;
import static org.codefromheaven.service.settings.SettingsServiceBase.loadSettingsFile;

public class VisibilitySettings {

    private final SettingsDTO hiddenElements;
    private final SettingsDTO visibleElements;

    public VisibilitySettings() {
        this.hiddenElements = loadVisibleElementSettings(FileType.HIDDEN_ELEMENTS_SETTINGS);
        this.visibleElements = loadVisibleElementSettings(FileType.VISIBLE_ELEMENTS_SETTINGS);
    }

    public boolean isVisible(ButtonDTO button) {
        boolean visibleAsDefault = button.isVisibleAsDefault();
        if (visibleAsDefault) {
            return doesNotExistMatchingSetting(hiddenElements, button);
        } else {
            return doesExistMatchingSetting(visibleElements, button);
        }
    }

    public void saveSettings() {
        SettingsServiceBase.saveSettings(FileType.HIDDEN_ELEMENTS_SETTINGS, hiddenElements);
        SettingsServiceBase.saveSettings(FileType.VISIBLE_ELEMENTS_SETTINGS, visibleElements);
    }

    public void updateVisibilitySetting(ButtonDTO button, boolean visible) {
        removeMatchingSetting(hiddenElements, button);
        removeMatchingSetting(visibleElements, button);

        boolean visibleAsDefault = button.isVisibleAsDefault();
        SettingsDTO settingsToUpdate = null;
        if (visible && !visibleAsDefault) {
            settingsToUpdate = visibleElements;
        } else if (!visible && visibleAsDefault) {
            settingsToUpdate = hiddenElements;
        }

        if (settingsToUpdate != null) {
            addMatchingSetting(settingsToUpdate, button);
        }
    }

    private static void addMatchingSetting(SettingsDTO settings, ButtonDTO button) {
        settings.getSettings().add(new KeyValueDTO(button.getKey().getStringKey(), button.getKey().buttonName(), ""));
    }

    private static void removeMatchingSetting(SettingsDTO settings, ButtonDTO button) {
        settings.getSettings().removeIf(elem -> isMatchingSetting(elem, button.getKey()));
    }

    private static boolean doesExistMatchingSetting(SettingsDTO settings, ButtonDTO button) {
        return settings.getSettings().stream().anyMatch(elem -> isMatchingSetting(elem, button.getKey()));
    }

    private static boolean doesNotExistMatchingSetting(SettingsDTO settings, ButtonDTO button) {
        return settings.getSettings().stream().noneMatch(elem -> isMatchingSetting(elem, button.getKey()));
    }

    private static boolean isMatchingSetting(KeyValueDTO keyValue, VisibilitySettingKey visibilitySettingKey) {
        return keyValue.getKey().equals(visibilitySettingKey.getStringKey()) && keyValue.getValue().equals(visibilitySettingKey.buttonName());
    }

    private static SettingsDTO loadVisibleElementSettings(FileType fileType) {
        boolean presentMyOwnSettings = isPresentMyOwnSettingFile(fileType);
        if (presentMyOwnSettings) {
            return loadSettingsFile(fileType);
        }
        return new SettingsDTO();
    }

}
