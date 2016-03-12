//package com.intellectualcrafters.plot;
//
//import static com.intellectualcrafters.plot.PS.log;
//
//import com.google.gson.Gson;
//import com.google.gson.annotations.SerializedName;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.net.URL;
//import java.util.List;
//
//public class UpdaterTest {
//
//    @org.junit.Test
//    public void getUpdate() throws Exception {
//        String str = null;
//        BufferedReader reader = null;
//        try {
//            URL url = new URL("https://api.github.com/repos/IntellectualSites/PlotSquared/releases/latest");
//            reader = new BufferedReader(new InputStreamReader(url.openStream()));
//            StringBuilder buffer = new StringBuilder();
//            int read;
//            char[] chars = new char[1024];
//            while ((read = reader.read(chars)) != -1) {
//                buffer.append(chars, 0, read);
//            }
//
//            str = buffer.toString();
//        } catch (IOException e) {
//            log("&dCould not check for updates (0)");
//            e.printStackTrace();
//        } finally {
//            try {
//                if (reader != null) {
//                    reader.close();
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        if (str == null) {
//            return;
//        }
//        Gson gson = new Gson();
//        Release release = gson.fromJson(str, Release.class);
//        System.out.println(release.name);
//        for (Release.Assets asset : release.assets) {
//            System.out.println(asset.name);
//            System.out.println(asset.downloadUrl);
//        }
//    }
//    private static class Release {
//        String name;
//        List<Assets> assets;
//        private static class Assets {
//            String name;
//            @SerializedName("browser_download_url") String downloadUrl;
//        }
//
//    }
//
//}