package com.intellectualcrafters.json;

import java.util.Iterator;

/**
 * Convert an HTTP header to a JSONObject and back.
 *
 * @author JSON.org
 * @version 2014-05-03
 */
public class HTTP {
    /**
     * Carriage return/line feed.
     */
    public static final String CRLF = "\r\n";
    
    public static JSONObject toJSONObject(final String string) throws JSONException {
        final JSONObject jo = new JSONObject();
        final HTTPTokener x = new HTTPTokener(string);
        String token;
        token = x.nextToken();
        if (token.toUpperCase().startsWith("HTTP")) {
            // Response
            jo.put("HTTP-Version", token);
            jo.put("Status-Code", x.nextToken());
            jo.put("Reason-Phrase", x.nextTo('\0'));
            x.next();
        } else {
            // Request
            jo.put("Method", token);
            jo.put("Request-URI", x.nextToken());
            jo.put("HTTP-Version", x.nextToken());
        }
        // Fields
        while (x.more()) {
            final String name = x.nextTo(':');
            x.next(':');
            jo.put(name, x.nextTo('\0'));
            x.next();
        }
        return jo;
    }
    
    /**
     * Convert a JSONObject into an HTTP header. A request header must contain
     *
     *
     * <pre>
     * {
     *    Method: "POST" (for example),
     *    "Request-URI": "/" (for example),
     *    "HTTP-Version": "HTTP/1.1" (for example)
     * }
     * </pre>
     *
     * A response header must contain
     *
     *
     * <pre>
     * {
     *    "HTTP-Version": "HTTP/1.1" (for example),
     *    "Status-Code": "200" (for example),
     *    "Reason-Phrase": "OK" (for example)
     * }
     * </pre>
     *
     * Any other members of the JSONObject will be output as HTTP fields. The result will end with two CRLF pairs.
     *
     * @param jo A JSONObject
     *
     * @return An HTTP header string.
     *
     * @throws JSONException if the object does not contain enough information.
     */
    public static String toString(final JSONObject jo) throws JSONException {
        final Iterator<String> keys = jo.keys();
        String string;
        final StringBuilder sb = new StringBuilder();
        if (jo.has("Status-Code") && jo.has("Reason-Phrase")) {
            sb.append(jo.getString("HTTP-Version"));
            sb.append(' ');
            sb.append(jo.getString("Status-Code"));
            sb.append(' ');
            sb.append(jo.getString("Reason-Phrase"));
        } else if (jo.has("Method") && jo.has("Request-URI")) {
            sb.append(jo.getString("Method"));
            sb.append(' ');
            sb.append('"');
            sb.append(jo.getString("Request-URI"));
            sb.append('"');
            sb.append(' ');
            sb.append(jo.getString("HTTP-Version"));
        } else {
            throw new JSONException("Not enough material for an HTTP header.");
        }
        sb.append(CRLF);
        while (keys.hasNext()) {
            string = keys.next();
            if (!"HTTP-Version".equals(string) && !"Status-Code".equals(string) && !"Reason-Phrase".equals(string) && !"Method".equals(string) && !"Request-URI".equals(string) && !jo.isNull(string)) {
                sb.append(string);
                sb.append(": ");
                sb.append(jo.getString(string));
                sb.append(CRLF);
            }
        }
        sb.append(CRLF);
        return sb.toString();
    }
}
