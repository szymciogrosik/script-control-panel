package org.codefromheaven.helpers;

import org.codefromheaven.resources.FileNamesLoader;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppVersionUtils {

    public AppVersionUtils() {
    }

    public static String getAppVersion() {
        Properties properties = new Properties();
        try (InputStream input = FileNamesLoader.getResourceAsStream("version.properties")) {
            if (input == null) {
                System.out.println("Sorry, unable to find version.properties");
                return "Unknown";
            }
            properties.load(input);
            return properties.getProperty("version");
        } catch (IOException ex) {
            ex.printStackTrace();
            return "Unknown";
        }
    }

}
