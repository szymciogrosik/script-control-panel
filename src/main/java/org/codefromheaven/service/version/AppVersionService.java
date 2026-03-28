package org.codefromheaven.service.version;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.codefromheaven.dto.Link;
import org.codefromheaven.dto.release.GitHubRelease;
import org.codefromheaven.resources.FileNamesLoader;
import org.codefromheaven.service.network.NetworkService;
import org.codefromheaven.service.settings.SettingsService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.Properties;

@Service
public class AppVersionService {

    public static final String APP_NAME = "ScriptControlPanel-Win";
    public static final String ZIP_NAME = APP_NAME + ".zip";

    private GitHubRelease gitHubRelease = null;
    private final NetworkService networkService;
    private final SettingsService settingsService;
    private static String lastETag = null;
    private static GitHubRelease cachedLatestRelease = null;

    @Autowired
    public AppVersionService(NetworkService networkService, SettingsService settingsService) {
        this.networkService = networkService;
        this.settingsService = settingsService;
    }

    public String getCurrentVersion() {
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

    public boolean isNewVersionAvailable() {
        return !getCurrentVersion().equals(getLatestVersion());
    }

    public String getLatestVersion() {
        Optional<GitHubRelease> release = getLatestRelease();
        if (release.isPresent()) {
            return release.get().getTagName();
        } else {
            return getCurrentVersion();
        }
    }

    public void checkForUpdates() {
        gitHubRelease = fetchLatestReleaseOrPreRelease().orElse(null);
    }

    public String getLatestJarDownloadUrl() {
        for (GitHubRelease.GitHubAsset asset : getLatestRelease().get().getAssets()) {
            if (asset.getName().equals(ZIP_NAME)) {
                return asset.getBrowserDownloadUrl();
            }
        }
        throw new RuntimeException("No " + ZIP_NAME + " file found in the latest release");
    }

    private Optional<GitHubRelease> getLatestRelease() {
        if (gitHubRelease == null) {
            checkForUpdates();
        }
        return Optional.ofNullable(gitHubRelease);
    }

    public Optional<GitHubRelease> fetchLatestReleaseOrPreRelease() {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(Link.API_LATEST_RELEASE_JSON.getUrl());
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");

            if (lastETag != null) {
                connection.setRequestProperty("If-None-Match", lastETag);
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_NOT_MODIFIED) {
                networkService.setNetworkPresent();
                return Optional.ofNullable(cachedLatestRelease);
            }

            String newETag = connection.getHeaderField("ETag");
            if (newETag != null && !newETag.isEmpty()) {
                lastETag = newETag;
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(content.toString());

            boolean preReleaseEnabled = settingsService.isAllowedToDownloadPreReleases();
            JsonNode releaseNode = root.get("release");
            JsonNode preReleaseNode = root.get("preRelease");

            JsonNode targetNode = releaseNode;
            if (preReleaseEnabled && preReleaseNode != null && !preReleaseNode.isNull()) {
                if (releaseNode == null || releaseNode.isNull()) {
                    targetNode = preReleaseNode;
                } else {
                    Instant releaseDate = Instant.parse(releaseNode.get("releaseDate").asText());
                    Instant preReleaseDate = Instant.parse(preReleaseNode.get("releaseDate").asText());
                    if (preReleaseDate.isAfter(releaseDate)) {
                        targetNode = preReleaseNode;
                    }
                }
            }

            if (targetNode == null || targetNode.isNull()) {
                return Optional.empty();
            }

            GitHubRelease latestRelease = new GitHubRelease();
            latestRelease.setTagName(targetNode.get("version").asText());
            latestRelease.setCreatedAt(targetNode.get("releaseDate").asText());
            GitHubRelease.GitHubAsset asset = new GitHubRelease.GitHubAsset();
            asset.setBrowserDownloadUrl(targetNode.get("downloadUrl").asText());
            asset.setName(ZIP_NAME);
            latestRelease.setAssets(java.util.Collections.singletonList(asset));

            cachedLatestRelease = latestRelease;
            networkService.setNetworkPresent();
            return Optional.of(latestRelease);
        } catch (IOException e) {
            e.printStackTrace();
            if (networkService.isNetworkUnknown()) {
                networkService.showPopupNetworkNotPresent();
            }
            networkService.setNetworkNotPresent();
            return Optional.ofNullable(cachedLatestRelease);
        }
    }

}
