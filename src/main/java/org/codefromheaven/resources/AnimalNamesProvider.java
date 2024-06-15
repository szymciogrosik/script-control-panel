package org.codefromheaven.resources;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class AnimalNamesProvider {

    private AnimalNamesProvider() {}

    public static String getRandomAnimalName() {
        List<String> animals = AnimalNamesProvider.ALL;
        Random random = new Random();
        return animals.get(random.nextInt(animals.size()));
    }

    /*
    * It's really hard to load elements from directory in JAR so here is the output of parser app
    * */
    public static final List<String> ALL = Arrays.asList(
            "armadillo.png",
            "bat.png",
            "bee.png",
            "cat.png",
            "chameleon.png",
            "chick.png",
            "crab.png",
            "dinosaur.png",
            "dolphin.png",
            "elephant.png",
            "fox.png",
            "frog.png",
            "giraffe.png",
            "koala.png",
            "owl.png",
            "panda.png",
            "penguin.png",
            "raccoon.png",
            "rat.png",
            "sea-turtle.png",
            "snake.png",
            "squirrel.png",
            "starfish.png",
            "turtle.png",
            "whale.png"
    );

}
