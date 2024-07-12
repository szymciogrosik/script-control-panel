package org.codefromheaven.service.version;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.scene.control.Alert;
import org.codefromheaven.controller.PopupController;
import org.codefromheaven.dto.Link;
import org.codefromheaven.dto.release.GitHubRelease;
import org.codefromheaven.resources.FileNamesLoader;
import org.codefromheaven.service.settings.SettingsService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.util.Optional;
import java.util.Properties;

public class AppVersionService {

    public static final String APP_NAME = "script_control_panel.jar";
    public static final String TMP_NAME = "new_" + APP_NAME;

    private static GitHubRelease gitHubRelease = null;
    private static Boolean networkPresent = null;

    private AppVersionService() {
    }

    public static String getCurrentVersion() {
        Properties properties = new Properties();
        try (InputStream input = FileNamesLoader.getResourceAsStream("version.properties")) {
            if (input == null) {
                throw new RuntimeException("Sorry, unable to find version.properties");
            }
            properties.load(input);
            String versionString = properties.getProperty("version");
            return "v" + versionString;
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    public static boolean isNewVersionAvailable() {
        return !getCurrentVersion().equals(getLatestVersion());
    }

    public static String getLatestVersion() {
        Optional<GitHubRelease> release = getLatestRelease();
        if (release.isPresent()) {
            return release.get().getTagName();
        } else {
            return getCurrentVersion();
        }
    }

    public static void checkForUpdates() {
        gitHubRelease = fetchLatestReleaseOrPreRelease().orElse(null);
    }

    public static String getLatestJarDownloadUrl() {
        for (GitHubRelease.GitHubAsset asset : getLatestRelease().get().getAssets()) {
            if (asset.getName().equals(APP_NAME)) {
                return asset.getBrowserDownloadUrl();
            }
        }
        throw new RuntimeException("No " + APP_NAME + " file found in the latest release");
    }

    private static Optional<GitHubRelease> getLatestRelease() {
        if (gitHubRelease == null) {
            checkForUpdates();
        }
        return Optional.ofNullable(gitHubRelease);
    }

    public static Optional<GitHubRelease> fetchLatestReleaseOrPreRelease() {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(Link.API_ALL_RELEASES.getUrl()).openConnection();
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
            JsonNode releases = objectMapper.readTree(content.toString());

            GitHubRelease latestRelease = null;
            Instant latestReleaseDate = Instant.MIN;

            latestRelease = findLatestGitHubRelease(releases, latestReleaseDate, latestRelease, objectMapper);

            networkPresent = true;
            return Optional.of(latestRelease);
        } catch (IOException e) {
            e.printStackTrace();
            if (networkPresent == null) {
                showPopupNetworkNotPresent();
            }
            networkPresent = false;
            return Optional.empty();
        }
    }

    private static GitHubRelease findLatestGitHubRelease(JsonNode releases, Instant latestReleaseDate, GitHubRelease latestRelease, ObjectMapper objectMapper) throws JsonProcessingException {
        boolean preReleaseEnabled = SettingsService.isAllowedToDownloadPreReleases();
        for (JsonNode release : releases) {
            GitHubRelease tmpRelease = objectMapper.treeToValue(release, GitHubRelease.class);

            if (!preReleaseEnabled && tmpRelease.isPrerelease()) {
                continue;
            }

            Instant releaseDate = Instant.parse(tmpRelease.getCreatedAt());
            if (releaseDate.isAfter(latestReleaseDate)) {
                latestReleaseDate = releaseDate;
                latestRelease = tmpRelease;
            }
        }
        return latestRelease;
    }

    public static boolean isNetworkPresent() {
        return networkPresent != null && networkPresent;
    }

    public static void showPopupNetworkNotPresent() {
        PopupController.showPopup("Network not found, try check the updates later.",
                                  Alert.AlertType.ERROR);
    }

}
