package org.codefromheaven.service.gh;

import org.codefromheaven.resources.FileNamesLoader;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;

public class GithubService {

    private GithubService() {
    }

    public static Optional<String> getGhDownloadToken() {
        Properties properties = new Properties();
        try (InputStream input = FileNamesLoader.getResourceAsStream("gh.token.properties")) {
            if (input == null) {
                throw new RuntimeException("Sorry, unable to find version.properties");
            }
            properties.load(input);
            String token = properties.getProperty("ghReleaseDownloadToken");

            if (token == null || token.isEmpty()) {
                return Optional.empty();
            } else {
                return Optional.of(token);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    public static Optional<String> getGhGetReleases() {
        Properties properties = new Properties();
        try (InputStream input = FileNamesLoader.getResourceAsStream("gh.token.properties")) {
            if (input == null) {
                throw new RuntimeException("Sorry, unable to find version.properties");
            }
            properties.load(input);
            String token = properties.getProperty("ghReleaseFetchReleasesToken");

            if (token == null || token.isEmpty()) {
                return Optional.empty();
            } else {
                return Optional.of(token);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

}
