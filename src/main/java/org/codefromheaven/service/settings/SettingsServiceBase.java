package org.codefromheaven.service.settings;

import org.codefromheaven.dto.FileType;
import org.codefromheaven.dto.Setting;
import org.codefromheaven.dto.settings.SettingDTO;
import org.codefromheaven.dto.settings.SettingsDTO;
import org.codefromheaven.helpers.FileUtils;
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
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class SettingsServiceBase {

    private static final String DEFAULT_PREFIX = "default_";
    private static final String MY_OWN_PREFIX = "my_own_";
    private static final String CONFIG_FILE_EXTENSION = ".json";

    private static final String CONFIG_LOCATION = getConfigDir();
    private static final String CONFIG_DIR = CONFIG_LOCATION.isEmpty() ? "" : CONFIG_LOCATION + "/";

    public static SettingsDTO loadSettingsFile(FileType fileType) {
        Optional<SettingsDTO> myOwnSettings = loadSettingsFile(getMyOwnFileDir(fileType.name()));
        SettingsDTO defaultSettings = mergeDefaultSettings(
                loadSettingsFile(getDefaultFileDir(fileType.name())).orElse(new SettingsDTO()), fileType);

        if (myOwnSettings.isEmpty()) {
            return defaultSettings;
        }

        List<SettingDTO> settingsToReturn = new ArrayList<>();

        // Iterate through default setting and if you will be found my_own_value then
        // add it
        defaultSettings.getSettings().forEach(defaultSetting -> {
            Optional<SettingDTO> myOwn = myOwnSettings.get().getSettings().stream()
                    .filter(elem -> defaultSetting.getKey().equals(elem.getKey())).findFirst();
            if (myOwn.isPresent() && defaultSetting.isEditable()) {
                // Override my own description with the newest one from default
                myOwn.get().setDescription(defaultSetting.getDescription());
                myOwn.get().setType(defaultSetting.getType());
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

    private static String getConfigDir() {
        // Config dir cannot be editable
        return DefaultSettings.ALL.getSettings().stream()
                .filter(keyValue -> keyValue.getKey().equals(Setting.CONFIG_DIR.getName()))
                .findFirst().get().getValue();
    }

    private static SettingsDTO mergeDefaultSettings(SettingsDTO defaultSettingsFromFile, FileType fileType) {
        if (fileType != FileType.SETTINGS) {
            return defaultSettingsFromFile;
        }
        List<SettingDTO> keyValueList = new ArrayList<>();

        // 1. Default settings from the app
        DefaultSettings.ALL.getSettings().forEach(appDefault -> {
            Optional<SettingDTO> fromFile = defaultSettingsFromFile.getSettings().stream()
                    .filter(fileSetting -> fileSetting.getKey().equals(appDefault.getKey())).findFirst();

            if (fromFile.isPresent()) {
                SettingDTO fileSetting = fromFile.get();
                fileSetting.setType(appDefault.getType());
                fileSetting.setDescription(appDefault.getDescription());
                keyValueList.add(fileSetting);
            } else {
                keyValueList.add(new SettingDTO(
                        appDefault.getKey(),
                        appDefault.getValue(),
                        appDefault.getType(),
                        appDefault.getDescription(),
                        appDefault.isEditable()));
            }
        });

        // 2. Add diff from default config file
        defaultSettingsFromFile.getSettings().forEach(fileSetting -> {
            if (keyValueList.stream().noneMatch(existing -> existing.getKey().equals(fileSetting.getKey()))) {
                if (fileSetting.getType() == null) {
                    fileSetting.setType(org.codefromheaven.dto.settings.SettingType.TEXT);
                }
                keyValueList.add(fileSetting);
            }
        });

        return new SettingsDTO(keyValueList);
    }

    private static Optional<SettingsDTO> loadSettingsFile(String settingPath) {
        try {
            return Optional.of(JsonUtils.deserialize(new File(settingPath), SettingsDTO.class));
        } catch (FileNotFoundException e) {
            return Optional.empty();
        }
    }

    public static void saveSettings(FileType fileType, SettingsDTO customSettings) {
        FileUtils.createOrReplaceConfigDirectory();
        SettingsDTO settingsToSave = getCustomSettingsDifferentThanDefault(fileType, customSettings);
        Path path = Paths.get(getMyOwnFileDir(fileType.name()));
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            writer.write(JsonUtils.serialize(settingsToSave));
        } catch (IOException e) {
            throw new RuntimeException("Failed to save settings", e);
        }
    }

    public static SettingsDTO getCustomSettingsDifferentThanDefault(FileType fileType, SettingsDTO customSettings) {
        SettingsDTO defaultSettings = mergeDefaultSettings(
                loadSettingsFile(getDefaultFileDir(fileType.name())).orElse(new SettingsDTO()), fileType);
        return new SettingsDTO(
                customSettings.getSettings().stream()
                        .filter(setting -> defaultSettings.getSettings().stream()
                                .noneMatch(defaultSetting -> defaultSetting.getKey().equals(setting.getKey()) &&
                                        Objects.equals(defaultSetting.getValue(), setting.getValue())))
                        .collect(Collectors.toList()));
    }

    public static Optional<String> loadValue(String setting, FileType fileType) {
        SettingsDTO settingsList = loadSettingsFile(fileType);
        return settingsList.getSettings().stream()
                .filter(elem -> elem.getKey().equals(setting))
                .map(SettingDTO::getValue).findFirst();
    }

    public static boolean isPresentMyOwnSettingFile(FileType fileType) {
        try (BufferedReader ignored = new BufferedReader(new FileReader(getMyOwnFileDir(fileType.name())))) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static String getDefaultFileDir(String fileName) {
        return CONFIG_DIR + DEFAULT_PREFIX + getDefaultFileNameBase(fileName);
    }

    public static String getMyOwnFileDir(String fileName) {
        return CONFIG_DIR + MY_OWN_PREFIX + getDefaultFileNameBase(fileName);
    }

    private static String getDefaultFileNameBase(String fileName) {
        return fileName.toLowerCase() + CONFIG_FILE_EXTENSION;
    }

}
