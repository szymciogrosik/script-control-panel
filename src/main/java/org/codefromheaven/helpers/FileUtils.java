package org.codefromheaven.helpers;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class FileUtils {

    public static final String TMP_DIR = "tmp";
    public static final String CONFIG_DIR = "config";

    private FileUtils() {
    }

    public static void copyFileFromResourceToTmp(String resourcePath, String fileName) {
        String appDir = createOrReplaceTmpDirectory();
        createOrReplaceFile(appDir, resourcePath, fileName);
    }

    public static String createOrReplaceTmpDirectory() {
        String appDir = System.getProperty("user.dir") + File.separator + TMP_DIR;
        createOrReplaceDirectory(appDir);
        return appDir;
    }

    public static void createOrReplaceConfigDirectory() {
        String appDir = System.getProperty("user.dir") + File.separator + CONFIG_DIR;
        createOrReplaceDirectory(appDir);
    }

    private static void createOrReplaceDirectory(String appDir) {
        File tmpDir = new File(appDir);
        if (!tmpDir.exists()) {
            if (!tmpDir.mkdirs()) {
                throw new IllegalStateException("Failed to create directory: " + appDir);
            }
        } else if (!tmpDir.isDirectory()) {
            throw new IllegalStateException(appDir + " exists but is not a directory.");
        }
    }

    private static void createOrReplaceFile(String appDir, String resourcePath, String fileName) {
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

    public static String getCurrentPath() {
        // Get the location of the JAR file
        String jarPath = null;
        try {
            jarPath = FileUtils.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        // Convert to a File object to get the parent directory
        File jarFile = new File(jarPath);

        // Return the absolute path of the parent directory
        return jarFile.getParentFile().getAbsolutePath();
    }

}
