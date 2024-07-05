package org.codefromheaven.service.settings;

import org.codefromheaven.dto.FileType;
import org.codefromheaven.dto.settings.KeyValueDTO;
import org.codefromheaven.dto.settings.SettingsDTO;
import org.codefromheaven.dto.settings.VisibilitySettingKey;

public class HiddenElementSettingsService extends SettingsServiceBase {

    private static final FileType FILE_TYPE = FileType.HIDDEN_ELEMENTS_SETTINGS;

    private HiddenElementSettingsService() {}

    public static SettingsDTO loadVisibilitySettings() {
        boolean presentMyOwnSettings = isPresentMyOwnSettingFile(FILE_TYPE);
        if (presentMyOwnSettings) {
            return loadSettingsFile(FILE_TYPE);
        }
        return new SettingsDTO();
    }

    public static void updateVisibilitySetting(SettingsDTO visibilitySettings, VisibilitySettingKey visibilitySettingKey, boolean checked) {
        boolean settingPresentInFile = visibilitySettings
                .getSettings().stream().anyMatch(elem -> isMatchingSetting(elem, visibilitySettingKey));

        boolean settingPresentAndNotChecked = settingPresentInFile && !checked;
        boolean settingNotPresentAndChecked = !settingPresentInFile && checked;

        boolean settingPresentAndChecked = settingPresentInFile && checked;
        boolean settingNotPresentAndNotChecked = !settingPresentInFile && !checked;

        if (settingPresentAndNotChecked || settingNotPresentAndChecked) {
            return;
        }

        if (settingPresentAndChecked) {
            visibilitySettings.getSettings().removeIf(elem -> isMatchingSetting(elem, visibilitySettingKey));
        }

        if (settingNotPresentAndNotChecked) {
            visibilitySettings.getSettings().add(new KeyValueDTO(getKey(visibilitySettingKey), visibilitySettingKey.buttonName(), ""));
        }
    }

    public static void saveSettings(SettingsDTO settings) {
        saveSettings(FILE_TYPE, settings);
    }

    public static boolean isMatchingSetting(KeyValueDTO keyValue, VisibilitySettingKey visibilitySettingKey) {
        return keyValue.getKey().equals(getKey(visibilitySettingKey)) && keyValue.getValue().equals(visibilitySettingKey.buttonName());
    }

    public static String getKey(VisibilitySettingKey visibilitySettingKey) {
        return visibilitySettingKey.sectionName() + " - " + visibilitySettingKey.subSectionName() + " - " + visibilitySettingKey.buttonName();
    }

}
