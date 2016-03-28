package com.intellectualcrafters.plot;

import static com.intellectualcrafters.plot.PS.log;

import com.intellectualcrafters.json.JSONArray;
import com.intellectualcrafters.json.JSONObject;
import com.intellectualcrafters.plot.util.StringMan;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

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
        JSONObject release = new JSONObject(str);
        JSONArray assets = (JSONArray) release.get("assets");
        String downloadURL = String.format("PlotSquared-%s.jar", PS.get().getPlatform());
        for (int i = 0; i < assets.length(); i++) {
            JSONObject asset = assets.getJSONObject(i);
            String name = asset.getString("name");
            if (downloadURL.equals(name)) {
                try {
                    String[] split = release.getString("name").split("\\.");
                    int[] version;
                    if (split.length == 3) {
                        version = new int[]{Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2])};
                    } else {
                        version = new int[]{Integer.parseInt(split[0]), Integer.parseInt(split[1]), 0};
                    }
                    URL url = new URL(asset.getString("browser_download_url"));
                    // If current version >= update
                    if (PS.get().checkVersion(PS.get().getVersion(), version)) {
                        PS.debug("&7PlotSquared is already up to date!");
                        return null;
                    }
                    log("&6PlotSquared " + StringMan.join(split, ".") + " is available:");
                    log("&8 - &3Use: &7/plot update");
                    log("&8 - &3Or: &7" + downloadURL);
                    return url;
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    log("&dCould not check for updates (1)");
                    log("&7 - Manually check for updates: https://github.com/IntellectualSites/PlotSquared/releases");
                }
            }
        }
        log("You are running the latest version of PlotSquared");
        return null;
    }
}
