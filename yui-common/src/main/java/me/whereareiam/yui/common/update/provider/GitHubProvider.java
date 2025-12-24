package me.whereareiam.yui.common.update.provider;

import lombok.Data;
import me.whereareiam.configura.Config;
import me.whereareiam.configura.reader.ConfigReader;
import me.whereareiam.configura.type.Format;
import me.whereareiam.yui.model.update.UpdateSource;
import me.whereareiam.yui.update.UpdateProvider;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Update provider for GitHub repositories.
 * Fetches releases and commits from the GitHub API.
 */
@Component
public class GitHubProvider implements UpdateProvider {
    private static final ConfigReader JSON_READER = Config.reader(Format.JSON);
    private static final String PROVIDER_ID = "github";
    private static final String DEFAULT_API_BASE = "https://api.github.com";
    private static final String API_BASE_PROPERTY = "yui.update.github.base-url";

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public Optional<String> fetchLatest(UpdateSource source) throws IOException {
        String api = apiBase() + "/repos/" + source.getId() + "/releases/latest";

        try (InputStream in = request(api)) {
            GitHubRelease release = JSON_READER.decode(in, GitHubRelease.class);
            return Optional.ofNullable(release.tag_name);
        } catch (IOException e) {
            // If no releases exist, GitHub returns 404
            if (e.getMessage() != null && e.getMessage().contains("404")) {
                return Optional.empty();
            }
            throw e;
        }
    }

    @Override
    public List<String> fetchRecentUpdates(UpdateSource source, int limit) throws IOException {
        String api = apiBase() + "/repos/" + source.getId() + "/commits?per_page=" + limit;

        try (InputStream in = request(api)) {
            GitHubCommit[] commits = JSON_READER.decode(in, GitHubCommit[].class);
            return Arrays.stream(commits)
                    .map(c -> c.sha)
                    .collect(Collectors.toList());
        }
    }

    private InputStream request(String urlString) throws IOException {
        URL url = URI.create(urlString).toURL();

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(8_000);
        conn.setReadTimeout(8_000);

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new IOException("HTTP " + responseCode + " for " + urlString);
        }

        return conn.getInputStream();
    }

    private String apiBase() {
        String value = System.getProperty(API_BASE_PROPERTY, DEFAULT_API_BASE);
        if (value.endsWith("/")) {
            return value.substring(0, value.length() - 1);
        }
        return value;
    }

    @Data
    private static class GitHubRelease {
        private String tag_name;
    }

    @Data
    private static class GitHubCommit {
        private String sha;
    }
}
