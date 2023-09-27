package org.openjfx.service;

import org.openjfx.dto.BaseSetting;
import org.openjfx.dto.ElementType;
import org.openjfx.dto.InternalSetting;
import org.openjfx.resources.AnimalNamesProvider;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

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
            String currentAnimal = AnimalNamesProvider.getRandomAnimal();
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
}
