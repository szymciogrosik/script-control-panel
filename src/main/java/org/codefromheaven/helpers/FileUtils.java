package org.codefromheaven.helpers;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class FileUtils {

    private FileUtils() {
    }

    public static void copyFileFromResource(String resourcePath, String fileName) {
        String appDir = System.getProperty("user.dir");

        // Destination file path
        File destFile = new File(appDir, fileName);
        String fullPathToResource = "/" + resourcePath + "/" + fileName;
        try (InputStream resourceStream = FileUtils.class.getResourceAsStream(fullPathToResource)) {
            if (resourceStream == null) {
                throw new IllegalStateException("Resource not found: " + fullPathToResource);
            }

            // Copy the resource to the destination file
            try (OutputStream outputStream = Files.newOutputStream(destFile.toPath())) {
                Files.copy(resourceStream, destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            System.out.println("File copied to: " + destFile.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
