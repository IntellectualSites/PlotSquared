package com.intellectualcrafters.plot;

import static com.intellectualcrafters.plot.PS.log;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class Updater {

    private static String readUrl(String urlString) {
        BufferedReader reader = null;
        try {
            URL url = new URL(urlString);
            reader = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuilder buffer = new StringBuilder();
            int read;
            char[] chars = new char[1024];
            while ((read = reader.read(chars)) != -1) {
                buffer.append(chars, 0, read);
            }

            return buffer.toString();
        } catch (IOException e) {
            log("&dCould not check for updates (0)");
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static URL getUpdate() {
        String str = readUrl("https://api.github.com/repos/IntellectualSites/PlotSquared/releases/latest");
        Gson gson = new Gson();
        URL url = null;
        Release release = gson.fromJson(str, Release.class);
        String downloadURL = String.format("PlotSquared-%s%n.jar", PS.get().getPlatform());
        List<Release.Asset> assets = release.assets;
        for (Release.Asset asset : assets) {
            if (asset.name.equals(downloadURL)) {
                try {
                    url = new URL(asset.downloadUrl);
                    break;
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    log("&dCould not check for updates (0)");
                    log("&7 - Manually check for updates: https://github.com/IntellectualSites/PlotSquared/releases");
                }
            }
        }
        if (!PS.get().canUpdate(PS.get().config.getString("version"), release.name)) {
            PS.debug("&7PlotSquared is already up to date!");
            return null;
        }
        log("&6PlotSquared " + release.tagName + " is available:");
        log("&8 - &3Use: &7/plot update");
        log("&8 - &3Or: &7" + downloadURL);
        return url;
    }


    private static class Release {
        public String name;
        @SerializedName("tag_name") String tagName;
        List<Asset> assets;
        static class Asset {
            public String name;
            @SerializedName("browser_download_url") String downloadUrl;
        }

    }
}
