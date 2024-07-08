package org.codefromheaven.service.version;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.codefromheaven.dto.Link;
import org.codefromheaven.dto.release.GitHubRelease;
import org.codefromheaven.resources.FileNamesLoader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;

public class AppVersionService {

    public static final String APP_NAME = "script_control_panel.jar";
    public static final String TMP_NAME = "tmp_" + APP_NAME;

    private static GitHubRelease gitHubRelease = null;

    private AppVersionService() {
    }

    public static String getCurrentVersion() {
        Properties properties = new Properties();
        try (InputStream input = FileNamesLoader.getResourceAsStream("version.properties")) {
            if (input == null) {
                throw new RuntimeException("Sorry, unable to find version.properties");
            }
            properties.load(input);
            return properties.getProperty("version");
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    public static boolean isNewVersionAvailable() {
        return !getCurrentVersion().equals(getLatestVersion());
    }

    public static String getLatestVersion() {
        return getLatestRelease().getTagName();
    }

    public static void checkForUpdates() {
        gitHubRelease = fetchLatestRelease();
    }

    public static String getLatestJarDownloadUrl() {
        for (GitHubRelease.GitHubAsset asset : getLatestRelease().getAssets()) {
            if (asset.getName().equals(APP_NAME)) {
                return asset.getBrowserDownloadUrl();
            }
        }
        throw new RuntimeException("No " + APP_NAME + " file found in the latest release");
    }

    private static GitHubRelease getLatestRelease() {
        if (gitHubRelease == null) {
            checkForUpdates();
        }
        return gitHubRelease;
    }

    private static GitHubRelease fetchLatestRelease() {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(Link.API_LATEST_RELEASE.getUrl()).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json");

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();

            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(content.toString(), GitHubRelease.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
