package org.openjfx.service;

import org.openjfx.helpers.ImageLoader;
import javafx.scene.image.Image;
import org.openjfx.resources.AnimalsProvider;

import java.util.List;
import java.util.Random;

public class ImageProvider {

    private static final String ANIMAL_DIRECTORY_NAME = "animals";
    private static final String ANIMAL_RESOURCE_PATH = "/" + ANIMAL_DIRECTORY_NAME + "/";

    private ImageProvider() {}

    public static Image get(String path) {
        return ImageLoader.getImage(path);
    }

    public static Image getRandomAnimal() {
        return ImageLoader.getImage(getRandomAnimalPath());
    }

    private static String getRandomAnimalPath() {
        List<String> animals = AnimalsProvider.ALL;
        Random random = new Random();
        return ANIMAL_RESOURCE_PATH + animals.get(random.nextInt(animals.size()));
    }

}
