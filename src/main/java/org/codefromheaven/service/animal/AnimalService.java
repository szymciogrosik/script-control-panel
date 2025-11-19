package org.codefromheaven.service.animal;

import javafx.scene.image.Image;
import org.codefromheaven.dto.Setting;
import org.codefromheaven.dto.data.AnimalDTO;
import org.codefromheaven.helpers.ImageLoader;
import jakarta.annotation.PostConstruct;
import org.codefromheaven.dto.Setting;
import org.codefromheaven.dto.data.AnimalDTO;
import org.codefromheaven.helpers.ImageLoader;
import org.codefromheaven.resources.AnimalProvider;
import org.codefromheaven.service.settings.SettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AnimalService {

    private final SettingsService settingsService;

    @Autowired
    public AnimalService(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @PostConstruct
    public void init() {
        getCurrentAnimal();
    }

    public Image getCurrentAnimalImage() {
        return ImageLoader.getImage(getAnimalPath(getCurrentAnimal()));
    }

    private AnimalDTO getCurrentAnimal() {
        String name = settingsService.getAnimalImageFromSettingsOrAddIfDoesNotExist();
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
        settingsService.replaceOrCreateConfigVariable(Setting.IMAGE_NAME, newAnimal.getName());
    }

    private String getAnimalPath(AnimalDTO animal) {
        return "/" + animal.getImageType().getPath() + "/" + animal.getName();
    }

}
