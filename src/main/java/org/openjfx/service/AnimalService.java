package org.openjfx.service;

import javafx.scene.image.Image;
import org.openjfx.dto.InternalSetting;
import org.openjfx.helpers.ImageLoader;
import org.openjfx.resources.AnimalNamesProvider;

public class AnimalService {

    private static final String SHIBA_FILE_NAME = "/shiba.png";
    private static final String ANIMAL_DIRECTORY_NAME = "animals";
    private static final String ANIMAL_RESOURCE_PATH = "/" + ANIMAL_DIRECTORY_NAME + "/";

    public static AnimalService instance;
    public static String currentAnimal;

    private AnimalService() {
        currentAnimal = SettingsService.getAnimalImageFromConfigAndCreateMyOwnInternalSettingFileIfDoesNotExist();
    }

    public static AnimalService getInstance() {
        if (instance == null) {
            instance = new AnimalService();
        }
        return instance;
    }

    public String getCurrentAnimalName() {
        return currentAnimal;
    }

    public Image getCurrentAnimalImage() {
        return ImageLoader.getImage(getAnimalPath(getCurrentAnimalName()));
    }

    public Image getRandomAnimalImage() {
        return ImageLoader.getImage(getAnimalPath(AnimalNamesProvider.getRandomAnimalName()));
    }

    public void drawNewRandomAnimal() {
        currentAnimal = AnimalNamesProvider.getRandomAnimalName();
        replaceCurrentAnimal(currentAnimal);
    }

    private void replaceCurrentAnimal(String newAnimal) {
        SettingsService.replaceConfigVariable(InternalSetting.IMAGE_NAME, newAnimal);
    }

    private String getAnimalPath(String animal) {
        return ANIMAL_RESOURCE_PATH + animal;
    }

    public static Image getShiba() {
        return ImageLoader.getImage(SHIBA_FILE_NAME);
    }

}
