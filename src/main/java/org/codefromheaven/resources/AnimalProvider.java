package org.codefromheaven.resources;

import org.codefromheaven.dto.data.AnimalDTO;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Arrays;

public class AnimalProvider {

    private static final Map<ImageType, List<String>> ANIMALS_BY_TYPE;

    static {
        Map<ImageType, List<String>> map = new EnumMap<>(ImageType.class);

        for (ImageType type : ImageType.values()) {
            List<String> animals = loadAnimals(type);
            if (!animals.isEmpty()) {
                map.put(type, animals);
            }
        }

        ANIMALS_BY_TYPE = Collections.unmodifiableMap(map);
    }

    private static List<String> loadAnimals(ImageType type) {
        List<String> animals = new ArrayList<>();
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try {
            // Load all png files from the directory corresponding to the ImageType
            // classpath*:animals/standard/*.png
            String locationPattern = "classpath*:" + type.getPath() + "/*.png";
            Resource[] resources = resolver.getResources(locationPattern);

            animals = Arrays.stream(resources)
                    .map(Resource::getFilename)
                    .sorted()
                    .collect(Collectors.toList());

        } catch (IOException e) {
            System.err.println("Failed to load animals for type: " + type.name());
            e.printStackTrace();
        }
        return animals;
    }

    private AnimalProvider() {}

    public static AnimalDTO getNextAnimal() {
        return getNextAnimal(null);
    }

    public static AnimalDTO getNextAnimal(AnimalDTO currentAnimal) {
        ImageType currentType = determinateImageType();
        List<String> animals = getDeterminateAnimals(currentType);

        if (currentAnimal == null || currentAnimal.imageType() != currentType) {
            return new AnimalDTO(animals.get(0), currentType);
        }

        int oldIndex = animals.indexOf(currentAnimal.name());
        int nextIndex = oldIndex != -1 && oldIndex != (animals.size() - 1) ? oldIndex + 1 : 0;
        return new AnimalDTO(animals.get(nextIndex), currentType);
    }

    public static AnimalDTO findAnimalByNameOrReturnRandomIfNotPresent(String animalName) {
        ImageType currentType = determinateImageType();
        List<String> animals = getDeterminateAnimals(currentType);
        Optional<String> first = animals.stream().filter(a -> a.equals(animalName)).findFirst();
        return first.map(s -> new AnimalDTO(s, currentType)).orElseGet(AnimalProvider::getNextAnimal);
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
        if (isAuthorBirthday()) {
            return ImageType.BIRTHDAY;
        }
        if (isChristmasTime()) {
            return ImageType.CHRISTMAS;
        }
        return ImageType.STANDARD;
    }

    private static boolean isChristmasTime() {
        LocalDate now = LocalDate.now();
        boolean afterMiddleOfNovember = (now.getMonthValue() == 11 && now.getDayOfMonth() >= 15) || now.getMonthValue() == 12;
        boolean beforeMiddleOfJanuary = now.getMonthValue() == 1 && now.getDayOfMonth() <= 13;
        return afterMiddleOfNovember || beforeMiddleOfJanuary;
    }

    private static boolean isAuthorBirthday() {
        LocalDate now = LocalDate.now();
        return now.getMonthValue() == 1 && now.getDayOfMonth() == 14;
    }

}
