package org.codefromheaven.service.settings;

import org.codefromheaven.dto.settings.BaseSetting;
import org.codefromheaven.dto.FileType;
import org.codefromheaven.dto.InternalSetting;
import org.codefromheaven.dto.settings.KeyValueDTO;
import org.codefromheaven.dto.settings.SettingsDTO;
import org.codefromheaven.resources.AnimalNamesProvider;

import java.util.Collections;

public class InternalSettingsService extends SettingsServiceBase {

    private static final FileType FILE_TYPE = FileType.INTERNAL_SETTINGS;

    private static final InternalSetting IMAGE_SETTING = InternalSetting.IMAGE_NAME;

    private InternalSettingsService() {}

    public static String getAnimalImageFromConfigAndCreateMyOwnInternalSettingFileIfDoesNotExist() {
        boolean presentMyOwnSettings = isPresentMyOwnSettingFile(IMAGE_SETTING.getElementType());
        if (presentMyOwnSettings) {
            return loadValue(IMAGE_SETTING);
        } else {
            String animal = AnimalNamesProvider.getRandomAnimalName();
            SettingsDTO settings = new SettingsDTO(Collections.singletonList(new KeyValueDTO(IMAGE_SETTING.getName(), animal)));
            saveSettings(FILE_TYPE, settings);
            return animal;
        }
    }

    public static void replaceConfigVariable(BaseSetting elementToReplace, String newValue) {
        SettingsDTO settings = loadSettingsFile(FILE_TYPE);

        KeyValueDTO keyValue = settings.getSettings().stream()
                                       .filter(elem -> elem.getKey().equals(elementToReplace.getName()))
                                       .findFirst().get();
        keyValue.setValue(newValue);

        saveSettings(FILE_TYPE, settings);
    }

}
