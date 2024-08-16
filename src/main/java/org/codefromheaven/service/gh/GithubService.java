package org.codefromheaven.service.gh;

import org.codefromheaven.resources.FileNamesLoader;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;

public class GithubService {

    private static final String PROPERTIES_LOCAL_FILE_NAME_PREFIX = "local.";
    private static final String PROPERTIES_FILE_NAME = "gh.token.properties";
    private static final String GH_RELEASE_FETCH_RELEASES_TOKEN_NAME = "ghReleaseFetchReleasesToken";
    private static final String GH_RELEASE_DOWNLOAD_TOKEN_NAME = "ghReleaseDownloadToken";

    private GithubService() {
    }

    public static Optional<String> getGhGetReleasesToken() {
        Optional<String> localToken = getLocalToken(PROPERTIES_FILE_NAME, GH_RELEASE_FETCH_RELEASES_TOKEN_NAME);
        if (localToken.isPresent()) {
            return localToken;
        }
        return getToken(PROPERTIES_FILE_NAME, GH_RELEASE_FETCH_RELEASES_TOKEN_NAME);
    }

    public static Optional<String> getGhDownloadToken() {
        Optional<String> localToken = getLocalToken(PROPERTIES_FILE_NAME, GH_RELEASE_DOWNLOAD_TOKEN_NAME);
        if (localToken.isPresent()) {
            return localToken;
        }
        return getToken(PROPERTIES_FILE_NAME, GH_RELEASE_DOWNLOAD_TOKEN_NAME);
    }

    private static Optional<String> getLocalToken(String fileName, String tokenName) {
        try {
            return getToken(PROPERTIES_LOCAL_FILE_NAME_PREFIX + fileName, tokenName);
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    private static Optional<String> getToken(String fileName, String tokenName) {
        Properties properties = new Properties();
        try (InputStream input = FileNamesLoader.getResourceAsStream(fileName)) {
            if (input == null) {
                throw new RuntimeException("Sorry, unable to find version.properties");
            }
            properties.load(input);
            String token = properties.getProperty(tokenName);

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
