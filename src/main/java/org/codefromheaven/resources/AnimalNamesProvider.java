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
    );

}
