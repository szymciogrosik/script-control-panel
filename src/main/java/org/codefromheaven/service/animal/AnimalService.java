package org.codefromheaven.service.animal;

import javafx.scene.image.Image;
import org.codefromheaven.dto.Setting;
import org.codefromheaven.helpers.ImageLoader;
import org.codefromheaven.resources.AnimalNamesProvider;
import org.codefromheaven.service.settings.SettingsService;

public class AnimalService {

    private static final String SHIBA_FILE_NAME = "/shiba.png";
    private static final String ANIMAL_DIRECTORY_NAME = "animals";
    private static final String ANIMAL_RESOURCE_PATH = "/" + ANIMAL_DIRECTORY_NAME + "/";

    public static AnimalService instance;

    private AnimalService() {
        getCurrentAnimalName();
    }

    public static AnimalService getInstance() {
        if (instance == null) {
            instance = new AnimalService();
        }
        return instance;
    }

    public String getCurrentAnimalName() {
        return SettingsService.getAnimalImageFromSettingsOrAddIfDoesNotExist();
    }

    public Image getCurrentAnimalImage() {
        return ImageLoader.getImage(getAnimalPath(getCurrentAnimalName()));
    }

    public Image getRandomAnimalImage() {
        return ImageLoader.getImage(getAnimalPath(AnimalNamesProvider.getRandomAnimalName()));
    }

    public void replaceCurrentAnimalToRandomAnimal() {
        String newAnimal = AnimalNamesProvider.getRandomAnimalName();
        replaceCurrentAnimal(newAnimal);
    }

    private void replaceCurrentAnimal(String newAnimal) {
        SettingsService.replaceOrCreateConfigVariable(Setting.IMAGE_NAME, newAnimal);
    }

    private String getAnimalPath(String animal) {
        return ANIMAL_RESOURCE_PATH + animal;
    }

    public static Image getShiba() {
        return ImageLoader.getImage(SHIBA_FILE_NAME);
    }

}
