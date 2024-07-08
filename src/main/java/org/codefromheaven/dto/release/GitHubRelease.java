package org.codefromheaven.dto.release;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GitHubRelease {

    @JsonProperty("tag_name")
    private String tagName;

    @JsonProperty("assets")
    private List<GitHubAsset> assets;

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public List<GitHubAsset> getAssets() {
        return assets;
    }

    public void setAssets(List<GitHubAsset> assets) {
        this.assets = assets;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GitHubAsset {
        @JsonProperty("browser_download_url")
        private String browserDownloadUrl;

        @JsonProperty("name")
        private String name;

        public String getBrowserDownloadUrl() {
            return browserDownloadUrl;
        }

        public void setBrowserDownloadUrl(String browserDownloadUrl) {
            this.browserDownloadUrl = browserDownloadUrl;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

}
