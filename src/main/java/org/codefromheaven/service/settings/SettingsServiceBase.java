package org.codefromheaven.service.settings;

import org.codefromheaven.dto.settings.BaseSetting;
import org.codefromheaven.dto.FileType;
import org.codefromheaven.dto.settings.KeyValueDTO;
import org.codefromheaven.dto.settings.SettingsDTO;
import org.codefromheaven.helpers.JsonUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class SettingsServiceBase {

    private static final String DEFAULT_PREFIX = "default_";
    private static final String MY_OWN_PREFIX = "my_own_";
    private static final String CONFIG_FILE_EXTENSION = ".json";

    public static SettingsDTO loadSettingsFile(FileType fileType) {
        Optional<SettingsDTO> myOwnSettings = loadSettingsFile(getMyOwnFileName(fileType.name()));
        SettingsDTO defaultSettings = loadSettingsFile(getDefaultFileName(fileType.name())).get();

        if (myOwnSettings.isEmpty()) {
            return defaultSettings;
        }

        List<KeyValueDTO> settingsToReturn = new ArrayList<>();

        // Iterate through default setting and if you will be found my_own_value then add it
        defaultSettings.getSettings().forEach(defaultSetting -> {
            Optional<KeyValueDTO> myOwn =
                    myOwnSettings.get().getSettings().stream()
                                 .filter(elem -> defaultSetting.getKey().equals(elem.getKey())).findFirst();
            if (myOwn.isPresent()) {
                settingsToReturn.add(myOwn.get());
            } else {
                settingsToReturn.add(defaultSetting);
            }
        });

        // Add all my_own settings which was not present in default file
        myOwnSettings.get().getSettings().forEach(myOwnSetting -> {
            if (settingsToReturn.stream().noneMatch(elem -> myOwnSetting.getKey().equals(elem.getKey()))) {
                settingsToReturn.add(myOwnSetting);
            }
        });

        return new SettingsDTO(settingsToReturn);
    }

    private static Optional<SettingsDTO> loadSettingsFile(String settingPath) {
        try {
            return Optional.of(JsonUtils.deserialize(new File(settingPath), SettingsDTO.class));
        } catch (FileNotFoundException e) {
            return Optional.empty();
        }
    }

    public static void saveSettings(FileType fileType, SettingsDTO settings) {
        SettingsDTO settingsToSave = getCustomSettingsDifferentThanDefault(fileType, settings);
        Path path = Paths.get(getMyOwnFileName(fileType.name()));
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            writer.write(JsonUtils.serialize(settingsToSave));
        } catch (IOException e) {
            throw new RuntimeException("Failed to save settings", e);
        }
    }

    private static SettingsDTO getCustomSettingsDifferentThanDefault(FileType fileType, SettingsDTO allSettings) {
        SettingsDTO defaultSettings = loadSettingsFile(getDefaultFileName(fileType.name())).get();
        return new SettingsDTO(
                allSettings.getSettings().stream()
                           .filter(setting -> defaultSettings.getSettings().stream().noneMatch(defaultSetting -> defaultSetting.equals(setting)))
                           .collect(Collectors.toList()));
    }

    public static Optional<String> loadValue(BaseSetting setting) {
        return loadValue(setting, setting.getElementType());
    }

    private static Optional<String> loadValue(BaseSetting setting, FileType fileType) {
        SettingsDTO settingsList = loadSettingsFile(fileType);
        return settingsList.getSettings().stream()
                           .filter(elem -> elem.getKey().equals(setting.getName()))
                           .map(KeyValueDTO::getValue).findFirst();
    }

    protected static boolean isPresentMyOwnSettingFile(FileType fileType) {
        try (BufferedReader ignored = new BufferedReader(new FileReader(getMyOwnFileName(fileType.name())))) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static String getDefaultFileName(String fileName) {
        return DEFAULT_PREFIX + getDefaultFileNameBase(fileName);
    }

    public static String getMyOwnFileName(String fileName) {
        return MY_OWN_PREFIX + getDefaultFileNameBase(fileName);
    }

    private static String getDefaultFileNameBase(String fileName) {
        return fileName.toLowerCase() + CONFIG_FILE_EXTENSION;
    }

}
