package org.codefromheaven.helpers;

import javafx.scene.image.Image;

import java.net.URISyntaxException;

public class ImageLoader {

    private ImageLoader() {}

    public static Image getImage(String imageStr) {
        try {
            return new Image(ImageLoader.class.getResource(imageStr).toURI().toString());
        } catch (URISyntaxException ex) {
            throw new RuntimeException("Cannot fine image.", ex);
        }
    }

}