package com.nghiadark.fasthopper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;

public class UpdateChecker {

    private static final String GITHUB_API = "https://api.github.com/repos/NghiaDark211/FastHopper/releases/latest";

    private final FastHopper plugin;
    private String latestVersion;
    private boolean hasUpdate;
    private boolean checkFailed;
    private boolean checked;

    public UpdateChecker(FastHopper plugin) {
        this.plugin = plugin;
    }

    public void checkAsync() {
        plugin.getServer().getAsyncScheduler().runNow(plugin, task -> {
            try {
                performCheck();
            } catch (Exception e) {
                checkFailed = true;
                plugin.getLogger().warning("Update check failed: " + e.getMessage());
            }
        });
    }

    private void performCheck() throws Exception {
        URI uri = new URI(GITHUB_API);
        HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/vnd.github+json");
        conn.setRequestProperty("User-Agent", "FastHopper");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            checkFailed = true;
            return;
        }

        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }

        String json = response.toString();
        String searchKey = "\"tag_name\":\"";
        int start = json.indexOf(searchKey);
        if (start == -1) {
            checkFailed = true;
            return;
        }
        start += searchKey.length();
        int end = json.indexOf("\"", start);
        if (end == -1) {
            checkFailed = true;
            return;
        }
        String tagName = json.substring(start, end);
        latestVersion = tagName.startsWith("v") ? tagName.substring(1) : tagName;
        String currentVersion = plugin.getPluginMeta().getVersion();
        hasUpdate = !currentVersion.equals(latestVersion);
        checked = true;
        plugin.getLogger().info("Update check: current=" + currentVersion + " latest=" + latestVersion + " update=" + hasUpdate);
    }

    public boolean isUpdateAvailable() {
        return checked && hasUpdate;
    }

    public boolean isCheckFailed() {
        return checkFailed;
    }

    public boolean isChecked() {
        return checked;
    }

    public String getLatestVersion() {
        return latestVersion;
    }

    public String getUpdateMessage(boolean includePrefix) {
        if (checkFailed) {
            return (includePrefix ? plugin.getMessage("update-check-failed") : plugin.getRawMessage("update-check-failed"));
        }
        if (!checked) {
            return (includePrefix ? plugin.getMessage("update-checking") : plugin.getRawMessage("update-checking"));
        }
        if (hasUpdate) {
            String msg = includePrefix ? plugin.getMessage("update-available") : plugin.getRawMessage("update-available");
            return msg.replace("%latest%", latestVersion != null ? latestVersion : "?");
        }
        return (includePrefix ? plugin.getMessage("update-latest") : plugin.getRawMessage("update-latest"));
    }
}
