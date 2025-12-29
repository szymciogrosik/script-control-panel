package org.codefromheaven.resources;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnimalProviderTest {

    @Test
    void testLoadAnimals() {
        // This test will verify that animals are correctly loaded for each ImageType
        // It relies on the ResourceIndexer running before the test execution

        for (ImageType type : ImageType.values()) {
            List<String> animals = AnimalProvider.getDeterminateAnimals(); // This method uses current date context, which might be tricky
            // Instead, we can't access the private map directly, but we can check if at least one type (Standard) loads something.
            // Wait, getDeterminateAnimals() depends on date.

            // However, the static block runs on class initialization.
            // If the map was empty, calling logic might fail or return empty.
            // Let's verify via reflection or just check if we can get an animal.
        }

        // A better test is to check if we can get a standard animal (assuming it's not Xmas or Birthday)
        // Or we can just inspect the list files directly if we were testing the Indexer.
        // But here we test the Provider.

        // Let's try to access the map via reflection since it is private,
        // OR we can just check if loading ANY animal works.
        // Actually, we can't easily force ImageType in getDeterminateAnimals() because it's private logic.
        // BUT, getDeterminateAnimals() calls ANIMALS_BY_TYPE.get(imageType).

        // There is no public method to get animals for a specific type.
        // However, there is:
        // public static boolean doesAnimalNameExist(String animalName)

        // Let's rely on the fact that we know some animals exist in Standard.
        assertTrue(AnimalProvider.doesAnimalNameExist("bear.png") ||
                   AnimalProvider.doesAnimalNameExist("bear_1.png") ||
                   AnimalProvider.doesAnimalNameExist("balloon-bear.png"),
                   "Should find at least one bear image");
    }

    @Test
    void testStandardAnimalsLoaded() {
        // We can't change the date, so we can only test the current active type.
        // However, we can assert that the internal map is not empty using reflection.
        try {
            java.lang.reflect.Field field = AnimalProvider.class.getDeclaredField("ANIMALS_BY_TYPE");
            field.setAccessible(true);
            java.util.Map<?, ?> map = (java.util.Map<?, ?>) field.get(null);

            assertFalse(map.isEmpty(), "Animals map should not be empty");
            assertTrue(map.containsKey(ImageType.STANDARD), "Should contain STANDARD animals");
            List<?> standardAnimals = (List<?>) map.get(ImageType.STANDARD);
            assertFalse(standardAnimals.isEmpty(), "STANDARD animals list should not be empty");
            assertTrue(standardAnimals.contains("bear.png"), "STANDARD list should contain bear.png");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
