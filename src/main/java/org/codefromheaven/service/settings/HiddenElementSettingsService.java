package org.codefromheaven.service.settings;

import org.codefromheaven.dto.FileType;
import org.codefromheaven.dto.LoadedElementDTO;
import org.codefromheaven.dto.settings.KeyValueDTO;
import org.codefromheaven.dto.settings.SettingsDTO;

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

    public static void updateVisibilitySetting(SettingsDTO visibilitySettings, LoadedElementDTO loadedElement, boolean checked) {
        boolean settingPresentInFile = visibilitySettings
                .getSettings().stream().anyMatch(elem -> isMatchingSetting(elem, loadedElement));

        boolean settingPresentAndNotChecked = settingPresentInFile && !checked;
        boolean settingNotPresentAndChecked = !settingPresentInFile && checked;

        boolean settingPresentAndChecked = settingPresentInFile && checked;
        boolean settingNotPresentAndNotChecked = !settingPresentInFile && !checked;

        if (settingPresentAndNotChecked || settingNotPresentAndChecked) {
            return;
        }

        if (settingPresentAndChecked) {
            visibilitySettings.getSettings().removeIf(elem -> isMatchingSetting(elem, loadedElement));
        }

        if (settingNotPresentAndNotChecked) {
            visibilitySettings.getSettings().add(new KeyValueDTO(getKey(loadedElement), loadedElement.getButtonName(), ""));
        }
    }

    public static void saveSettings(SettingsDTO settings) {
        saveSettings(FILE_TYPE, settings);
    }

    public static boolean isMatchingSetting(KeyValueDTO keyValue, LoadedElementDTO loadedElement) {
        return keyValue.getKey().equals(getKey(loadedElement)) && keyValue.getValue().equals(loadedElement.getButtonName());
    }

    public static String getKey(LoadedElementDTO loadedElement) {
        return loadedElement.getSectionName() + " - " + loadedElement.getSubSectionName() + " - " + loadedElement.getButtonName();
    }

}
