package org.codefromheaven.service.settings;

import org.codefromheaven.dto.BaseSetting;
import org.codefromheaven.dto.FileType;
import org.codefromheaven.dto.InternalSetting;
import org.codefromheaven.resources.AnimalNamesProvider;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class InternalSettingsService extends SettingsServiceBase {

    private static final String INTERNAL_SETTING_FIRST_LINE = "VARIABLE_NAME;VARIABLE_VALUE";

    private InternalSettingsService() {}

    public static String getAnimalImageFromConfigAndCreateMyOwnInternalSettingFileIfDoesNotExist() {
        InternalSetting animalConfig = InternalSetting.IMAGE_NAME;
        boolean presentMyOwnSettings = isPresentMyOwnSettingFile(animalConfig.getElementType());
        if (presentMyOwnSettings) {
            return SettingsServiceBase.getVariable(animalConfig);
        } else {
            String currentAnimal = AnimalNamesProvider.getRandomAnimalName();
            createMyOwnInternalSettingsFile(currentAnimal);
            return currentAnimal;
        }
    }

    private static void createMyOwnInternalSettingsFile(String animal) {
        try {
            PrintWriter writer = new PrintWriter(FileType.INTERNAL_SETTINGS.getPersonalizedConfigName(), StandardCharsets.UTF_8);
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

        Path path = Paths.get(FileType.INTERNAL_SETTINGS.getPersonalizedConfigName());
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
