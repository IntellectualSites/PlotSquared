package com.intellectualcrafters.plot;

import static com.intellectualcrafters.plot.PS.log;

import com.intellectualcrafters.json.JSONArray;
import com.intellectualcrafters.json.JSONObject;
import com.intellectualcrafters.plot.util.StringMan;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

public class UpdaterTest {

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

    @Test
    public void getUpdate() throws Exception {
        String str = readUrl("https://api.github.com/repos/IntellectualSites/PlotSquared/releases/latest");
        JSONObject release = new JSONObject(str);
        JSONArray assets = (JSONArray) release.get("assets");
        System.out.println(assets.toString());
        String downloadURL = String.format("PlotSquared-%s.jar", "Bukkit");
        for (int i = 0; i < assets.length(); i++) {
            System.out.println(i);
            JSONObject asset = assets.getJSONObject(i);
            String name = asset.getString("name");
            System.out.println(name);
            System.out.println(downloadURL);
            if (downloadURL.equals(name)) {
                try {
                    String[] split = release.getString("name").split("\\.");
                    int[] version;
                    if (split.length == 3) {
                        version = new int[]{Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2])};
                    } else {
                        version = new int[]{Integer.parseInt(split[0]), Integer.parseInt(split[1]), 0};
                    }
                    System.out.println(Arrays.toString(version));
                    URL url = new URL(asset.getString("browser_download_url"));
                    // If current version >= update
                    if (checkVersion(new int[]{3, 3, 1}, version)) {
                        System.out.println("&7PlotSquared is already up to date!");
                        return;
                    }
                    System.out.println("&6PlotSquared " + StringMan.join(split, ".") + " is available:");
                    System.out.println("&8 - &3Use: &7/plot update");
                    System.out.println("&8 - &3Or: &7" + downloadURL);
                    return;
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    System.out.println("&dCould not check for updates (1)");
                    System.out.println("&7 - Manually check for updates: https://github.com/IntellectualSites/PlotSquared/releases");
                }
            }
        }
        System.out.println("You are running the latest version of PlotSquared");
        return;
    }

    public boolean checkVersion(int[] version, int... version2) {
        return version[0] > version2[0] || version[0] == version2[0] && version[1] > version2[1] || version[0] == version2[0]
                && version[1] == version2[1] && version[2] >= version2[2];
    }

}