package org.codefromheaven.service.animal;

import javafx.scene.image.Image;
import org.codefromheaven.dto.Setting;
import org.codefromheaven.dto.data.AnimalDTO;
import org.codefromheaven.helpers.ImageLoader;
import org.codefromheaven.resources.AnimalProvider;
import org.codefromheaven.service.settings.SettingsService;

public class AnimalService {

    public static AnimalService instance;

    private AnimalService() {
        getCurrentAnimal();
    }

    public static AnimalService getInstance() {
        if (instance == null) {
            instance = new AnimalService();
        }
        return instance;
    }

    public Image getCurrentAnimalImage() {
        return ImageLoader.getImage(getAnimalPath(getCurrentAnimal()));
    }

    private AnimalDTO getCurrentAnimal() {
        String name = SettingsService.getAnimalImageFromSettingsOrAddIfDoesNotExist();
        return AnimalProvider.findAnimalByNameOrReturnRandomIfNotPresent(name);
    }

    public Image getRandomAnimalImage() {
        return ImageLoader.getImage(getAnimalPath(AnimalProvider.getRandomAnimal()));
    }

    public void replaceCurrentAnimalToRandomAnimal() {
        AnimalDTO newAnimal = AnimalProvider.getRandomAnimal();
        replaceCurrentAnimal(newAnimal);
    }

    private void replaceCurrentAnimal(AnimalDTO newAnimal) {
        SettingsService.replaceOrCreateConfigVariable(Setting.IMAGE_NAME, newAnimal.getName());
    }

    private String getAnimalPath(AnimalDTO animal) {
        return "/" + animal.getImageType().getPath() + "/" + animal.getName();
    }

}
