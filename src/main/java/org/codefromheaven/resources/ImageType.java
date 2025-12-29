package org.codefromheaven.resources;

public enum ImageType {
    CHRISTMAS("animals/christmas"),
    BIRTHDAY("animals/birthday"),
    STANDARD("animals/standard");

    private final String path;

    ImageType(final String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
