package org.codefromheaven.resources;

public class AnimalsParser {

    private AnimalsParser() {}

    public static final String DIRECTORY = "animals";

    public static void main(String[] args) {
        System.out.println("----------------------------------------------------");
        for (String fileName : FileNamesLoader.getResourceFiles(DIRECTORY)) {
            System.out.println("\"" + fileName + "\",");
        }
        System.out.println("----------------------------------------------------");
    }

}
