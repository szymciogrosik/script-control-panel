package org.codefromheaven.dto.data;

import org.codefromheaven.resources.ImageType;

public class AnimalDTO {

    private String name;
    private ImageType imageType;

    public AnimalDTO(String name, ImageType imageType) {
        this.name = name;
        this.imageType = imageType;
    }

    public String getName() {
        return name;
    }

    public ImageType getImageType() {
        return imageType;
    }

}
