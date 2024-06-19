package org.codefromheaven.service.settings;

import org.codefromheaven.dto.FileType;
import org.codefromheaven.dto.settings.KeyValueDTO;
import org.codefromheaven.dto.settings.SettingsDTO;

public class InternalVisibilitySettingsService extends SettingsServiceBase {

    private static final FileType FILE_TYPE = FileType.INTERNAL_VISIBILITY_SETTINGS;

    private InternalVisibilitySettingsService() {}

    public static SettingsDTO loadVisibilitySettings() {
        boolean presentMyOwnSettings = isPresentMyOwnSettingFile(FILE_TYPE);
        if (presentMyOwnSettings) {
            return loadSettingsFile(FILE_TYPE);
        }
        return new SettingsDTO();
    }

    public static void updateVisibilitySetting(String section, String checkboxName, boolean checked) {
        SettingsDTO allSettings = loadVisibilitySettings();
        boolean settingPresentInFile = allSettings
                .getSettings().stream().anyMatch(elem -> isMatchingSetting(elem, section, checkboxName));

        boolean settingPresentAndNotChecked = settingPresentInFile && !checked;
        boolean settingNotPresentAndChecked = !settingPresentInFile && checked;

        boolean settingPresentAndChecked = settingPresentInFile && checked;
        boolean settingNotPresentAndNotChecked = !settingPresentInFile && !checked;

        if (settingPresentAndNotChecked || settingNotPresentAndChecked) {
            return;
        }

        if (settingPresentAndChecked) {
            allSettings.getSettings().removeIf(elem -> isMatchingSetting(elem, section, checkboxName));
        }

        if (settingNotPresentAndNotChecked) {
            allSettings.getSettings().add(new KeyValueDTO(section, checkboxName));
        }

        saveSettings(FILE_TYPE, allSettings);
    }

    public static boolean isMatchingSetting(KeyValueDTO keyValue, String section, String checkboxName) {
        return keyValue.getKey().equals(section) && keyValue.getValue().equals(checkboxName);
    }

}
