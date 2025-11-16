package org.codefromheaven.resources;

import java.util.List;

public class AnimalsParser {

    private AnimalsParser() {}

    public static void main(String[] args) {
        System.out.println("----------------------------------------------------");
        for (ImageType value : ImageType.values()) {
            List<String> files = FileNamesLoader.getResourceFiles(value.getPath());
            System.out.println("map.put(ImageType.valueOf(\"" + value.name() + "\"), Arrays.asList(");
            for (int i = 0; i < files.size(); i++) {
                String file = files.get(i);
                String comma = (i < files.size() - 1) ? "," : "";
                System.out.println("    \"" + file + "\"" + comma);
            }
            System.out.println("));\n");
        }
        System.out.println("----------------------------------------------------");
    }
}
