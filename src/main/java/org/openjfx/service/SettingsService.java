package org.openjfx.service;

import org.openjfx.dto.BaseSetting;
import org.openjfx.dto.ElementType;
import org.openjfx.dto.InternalSetting;
import org.openjfx.resources.AnimalNamesProvider;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class SettingsService {

    private static final String INTERNAL_SETTING_FIRST_LINE = "VARIABLE_NAME;VARIABLE_VALUE";
    private static final String DELIMITER = ";";

    private SettingsService() {}

    public static String getVariable(BaseSetting setting) {
        Optional<String> value = getVariableBase(setting, setting.getElementType().getPersonalizedConfigName());
        return value.orElseGet(() -> getVariableBase(setting, setting.getElementType().getDefaultFileName()).get());
    }

    public static boolean isPresentMyOwnSettingFile(ElementType elementType) {
        try (BufferedReader ignored = new BufferedReader(new FileReader(elementType.getPersonalizedConfigName()))) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private static Optional<String> getVariableBase(BaseSetting setting, String settingCsvPath) {
        try (BufferedReader br = new BufferedReader(new FileReader(settingCsvPath))) {
            String line;
            // skip first line
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] values = line.split(DELIMITER);
                if (setting.getName().equals(values[0])) {
                    return Optional.of(values[1]);
                }
            }
        } catch (IOException e) {
            return Optional.empty();
        }
        return Optional.empty();
    }

    public static String getAnimalImageFromConfigAndCreateMyOwnInternalSettingFileIfDoesNotExist() {
        InternalSetting animalConfig = InternalSetting.IMAGE_NAME;
        boolean presentMyOwnSettings = SettingsService.isPresentMyOwnSettingFile(animalConfig.getElementType());
        if (presentMyOwnSettings) {
            return SettingsService.getVariable(animalConfig);
        } else {
            String currentAnimal = AnimalNamesProvider.getRandomAnimalName();
            createMyOwnInternalSettingsFile(currentAnimal);
            return currentAnimal;
        }
    }

    private static void createMyOwnInternalSettingsFile(String animal) {
        try {
            PrintWriter writer = new PrintWriter(ElementType.INTERNAL_SETTINGS.getPersonalizedConfigName(), StandardCharsets.UTF_8);
            writer.println(INTERNAL_SETTING_FIRST_LINE);
            writer.println(getSettingConfigLine(InternalSetting.IMAGE_NAME, animal));
            writer.close();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static String getSettingConfigLine(BaseSetting baseSetting, String animal) {
        return baseSetting.getName() + ";" + animal;
    }

    public static void replaceConfigVariable(BaseSetting elementToReplace, String newValue) {
        String currentAnimal = getAnimalImageFromConfigAndCreateMyOwnInternalSettingFileIfDoesNotExist();

        Path path = Paths.get(ElementType.INTERNAL_SETTINGS.getPersonalizedConfigName());
        Charset charset = StandardCharsets.UTF_8;

        String content = null;
        try {
            content = Files.readString(path, charset);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        content = content.replaceAll(getSettingConfigLine(elementToReplace, currentAnimal), getSettingConfigLine(elementToReplace, newValue));
        try {
            Files.write(path, content.getBytes(charset));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void createMyOwnInternalVisibilitySettingsFile() {
        try {
            PrintWriter writer = new PrintWriter(ElementType.INTERNAL_VISIBILITY_SETTINGS.getPersonalizedConfigName(), StandardCharsets.UTF_8);
            writer.println(INTERNAL_SETTING_FIRST_LINE);
            writer.close();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static Map<String, Map<String, Boolean>> loadVisibilitySettings() {
        Map<String, Map<String, Boolean>> settings = new HashMap<>();
        boolean presentMyOwnSettings = SettingsService.isPresentMyOwnSettingFile(ElementType.INTERNAL_VISIBILITY_SETTINGS);
        if (!presentMyOwnSettings) {
            createMyOwnInternalVisibilitySettingsFile();
        }
        Path path = Paths.get(ElementType.INTERNAL_VISIBILITY_SETTINGS.getPersonalizedConfigName());
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            reader.readLine(); // Skip header
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length == 2) {
                    String section = parts[0];
                    Map<String, Boolean> checkboxStates = Arrays.stream(parts[1].split(","))
                                                                .map(s -> s.split("="))
                                                                .filter(a -> a.length == 2)  // Ensure there are two elements
                                                                .collect(Collectors.toMap(a -> a[0], a -> Boolean.parseBoolean(a[1]), (oldValue, newValue) -> newValue, HashMap::new));
                    settings.put(section, checkboxStates);
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to load settings: " + e.getMessage());
        }
        return settings;
    }

    public static void updateVisibilitySetting(String section, String checkboxName, boolean isChecked) {
        Map<String, Map<String, Boolean>> allSettings = loadVisibilitySettings();
        Map<String, Boolean> sectionSettings = allSettings.computeIfAbsent(section, k -> new HashMap<>());

        if (isChecked) {
            sectionSettings.remove(checkboxName); // Remove if checked
        } else {
            sectionSettings.put(checkboxName, false); // Only store if unchecked
        }

        saveVisibilitySettings(allSettings);
    }

    public static void saveVisibilitySettings(Map<String, Map<String, Boolean>> settings) {
        Path path = Paths.get(ElementType.INTERNAL_VISIBILITY_SETTINGS.getPersonalizedConfigName());
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            writer.write(INTERNAL_SETTING_FIRST_LINE);
            writer.newLine();
            for (Map.Entry<String, Map<String, Boolean>> entry : settings.entrySet()) {
                String section = entry.getKey();
                String values = entry.getValue().entrySet().stream()
                                     .map(e -> e.getKey() + "=" + e.getValue())
                                     .collect(Collectors.joining(","));
                if (!values.isEmpty()) {
                    writer.write(section + DELIMITER + values);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to save settings", e);
        }
    }


}
