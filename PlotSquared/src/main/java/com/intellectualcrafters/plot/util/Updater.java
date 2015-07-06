package com.intellectualcrafters.plot.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

public class Updater {
    public Updater() {
        // https://www.spigotmc.org/resources/plotsquared.1177/history
        try {
            
            String resource = "plotsquared.1177";
            String url = "https://www.spigotmc.org/resources/" + resource + "/history";
            String download = "<a href=\"resources/" + resource + "/download?version=";
            URL history = new URL(url);
            BufferedReader in = new BufferedReader(new InputStreamReader(history.openStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null)
                if (inputLine.length() > download.length() && inputLine.startsWith(download)) {
                    System.out.println("===========================");
                    System.out.println("FOUND LINE");
                    System.out.println("===========================");
                }
                System.out.println(inputLine);
            in.close();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
