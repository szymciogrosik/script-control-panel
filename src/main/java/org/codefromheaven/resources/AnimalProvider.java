package org.codefromheaven.resources;

import org.codefromheaven.dto.data.AnimalDTO;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

public class AnimalProvider {

    private static final Map<ImageType, List<String>> ANIMALS_BY_TYPE;

    static {
        Map<ImageType, List<String>> map = new EnumMap<>(ImageType.class);

        // REPLACE HERE BY GENERATED CODE - START
        map.put(ImageType.valueOf("STANDARD"), Arrays.asList(
                "armadillo.png",
                "axolotl.png",
                "bat.png",
                "bear.png",
                "bee.png",
                "boar.png",
                "cat.png",
                "chameleon.png",
                "chick.png",
                "crab.png",
                "crocodile.png",
                "dinosaur.png",
                "diplodocus.png",
                "dolphin.png",
                "elephant.png",
                "fox.png",
                "frog.png",
                "giraffe.png",
                "koala.png",
                "octopus.png",
                "owl.png",
                "panda.png",
                "penguin.png",
                "raccoon.png",
                "rat.png",
                "sea-turtle.png",
                "sheep.png",
                "shrimp.png",
                "snake.png",
                "squirrel.png",
                "starfish.png",
                "turtle.png",
                "whale.png",
                "wolf.png"
        ));

        map.put(ImageType.valueOf("CHRISTMAS"), Arrays.asList(
                "bear_1.png",
                "bear_2.png",
                "bear_3.png",
                "bear_4.png",
                "bear_5.png",
                "bear_6.png",
                "bird_1.png",
                "bird_2.png",
                "bird_3.png",
                "bunny.png",
                "cat_1.png",
                "cat_2.png",
                "cat_3.png",
                "cat_4.png",
                "dachshund.png",
                "deer_1.png",
                "deer_2.png",
                "dog_1.png",
                "dog_2.png",
                "fox.png",
                "hedgehog.png",
                "horse.png",
                "mouse_1.png",
                "mouse_2.png",
                "penguin.png",
                "polar-bear.png",
                "racoon.png",
                "reindeer.png",
                "rudolf.png",
                "sea-star.png",
                "shark.png"
        ));
        // REPLACE HERE BY GENERATED CODE - END

        ANIMALS_BY_TYPE = Collections.unmodifiableMap(map);
    }

    private AnimalProvider() {}

    public static AnimalDTO getRandomAnimal() {
        ImageType currentType = determinateImageType();
        List<String> animals = getDeterminateAnimals(currentType);
        Random random = new Random();
        return new AnimalDTO(animals.get(random.nextInt(animals.size())), currentType);
    }

    public static AnimalDTO findAnimalByNameOrReturnRandomIfNotPresent(String animalName) {
        ImageType currentType = determinateImageType();
        List<String> animals = getDeterminateAnimals(currentType);
        Optional<String> first = animals.stream().filter(a -> a.equals(animalName)).findFirst();
        return first.map(s -> new AnimalDTO(s, currentType)).orElseGet(AnimalProvider::getRandomAnimal);
    }

    public static List<String> getDeterminateAnimals() {
        return getDeterminateAnimals(determinateImageType());
    }

    private static List<String> getDeterminateAnimals(ImageType imageType) {
        return ANIMALS_BY_TYPE.get(imageType);
    }

    public static boolean doesAnimalNameExist(String animalName) {
        return ANIMALS_BY_TYPE.get(determinateImageType()).contains(animalName);
    }

    private static ImageType determinateImageType() {
        if (isChristmasTime()) {
            return ImageType.CHRISTMAS;
        } else {
            return ImageType.STANDARD;
        }
    }

    private static boolean isChristmasTime() {
        LocalDate now = LocalDate.now();
        boolean afterMiddleOfNovember = now.getMonthValue() >= 11 && now.getDayOfMonth() >= 15;
        boolean beforeMiddleOfJanuary = now.getMonthValue() == 1 && now.getDayOfMonth() <= 15;
        return afterMiddleOfNovember || beforeMiddleOfJanuary;
    }

}
