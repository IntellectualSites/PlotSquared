package com.intellectualcrafters.plot.util;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HastebinUtility {

    public static final String BIN_URL = "http://hastebin.com/documents", USER_AGENT = "Mozilla/5.0";
    public static final Pattern PATTERN = Pattern.compile("\\{\"key\":\"([\\S\\s]*)\"\\}");

    public static String upload(final String string) throws IOException {
        URL url = new URL(BIN_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setRequestProperty("User-Agent", USER_AGENT);
        connection.setDoOutput(true);

        DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
        outputStream.write(string.getBytes());
        outputStream.flush();
        outputStream.close();

        BufferedReader in = new BufferedReader(
                new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        Matcher matcher = PATTERN.matcher(response.toString());
        if (matcher.matches()) {
            return "http://hastebin.com/" + matcher.group(1);
        } else {
            throw new RuntimeException("Couldn't read response!");
        }
    }

    public static String upload(final File file) throws IOException {
        StringBuilder content = new StringBuilder();
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        while ((line = reader.readLine()) != null) {
            content.append(line).append("\n");
        }
        reader.close();
        return upload(content.toString());
    }

}

