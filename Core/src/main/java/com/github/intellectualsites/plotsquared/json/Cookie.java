package com.github.intellectualsites.plotsquared.json;

/**
 * Convert a web browser cookie specification to a JSONObject and back. JSON and Cookies are both notations for
 * name/value pairs.
 *
 * @author JSON.org
 * @version 2014-05-03
 */
public class Cookie {
    /**
     * Produce a copy of a string in which the characters '+', '%', '=', ';' and control characters are replaced with
     * "%hh". This is a gentle form of URL encoding, attempting to cause as little distortion to the string as possible.
     * The characters '=' and ';' are meta characters in cookies. By convention, they are escaped using the
     * URL-encoding. This is only a convention, not a standard. Often, cookies are expected to have encoded values. We
     * encode '=' and ';' because we must. We encode '%' and '+' because they are meta characters in URL encoding.
     *
     * @param string The source string.
     * @return The escaped result.
     */
    public static String escape(final String string) {
        char c;
        final String s = string.trim();
        final int length = s.length();
        final StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i += 1) {
            c = s.charAt(i);
            if ((c < ' ') || (c == '+') || (c == '%') || (c == '=') || (c == ';')) {
                sb.append('%');
                sb.append(Character.forDigit((char) ((c >>> 4) & 0x0f), 16));
                sb.append(Character.forDigit((char) (c & 0x0f), 16));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Convert a cookie specification string into a JSONObject. The string will contain a name value pair separated by
     * '='. The name and the value will be unescaped, possibly converting '+' and '%' sequences. The cookie properties
     * may follow, separated by ';', also represented as name=value (except the secure property, which does not have a
     * value). The name will be stored under the key "name", and the value will be stored under the key "value". This
     * method does not do checking or validation of the parameters. It only converts the cookie string into a
     * JSONObject.
     *
     * @param string The cookie specification string.
     * @return A JSONObject containing "name", "value", and possibly other members.
     * @throws JSONException
     */
    public static JSONObject toJSONObject(final String string) throws JSONException {
        String name;
        final JSONObject jo = new JSONObject();
        Object value;
        final JSONTokener x = new JSONTokener(string);
        jo.put("name", x.nextTo('='));
        x.next('=');
        jo.put("value", x.nextTo(';'));
        x.next();
        while (x.more()) {
            name = unescape(x.nextTo("=;"));
            if (x.next() != '=') {
                if (name.equals("secure")) {
                    value = Boolean.TRUE;
                } else {
                    throw x.syntaxError("Missing '=' in cookie parameter.");
                }
            } else {
                value = unescape(x.nextTo(';'));
                x.next();
            }
            jo.put(name, value);
        }
        return jo;
    }

    /**
     * Convert a JSONObject into a cookie specification string. The JSONObject must contain "name" and "value" members.
     * If the JSONObject contains "expires", "domain", "path", or "secure" members, they will be appended to the cookie
     * specification string. All other members are ignored.
     *
     * @param jo A JSONObject
     * @return A cookie specification string
     * @throws JSONException
     */
    public static String toString(final JSONObject jo) throws JSONException {
        final StringBuilder sb = new StringBuilder();
        sb.append(escape(jo.getString("name"))).append("=").append(escape(jo.getString("value")));
        if (jo.has("expires")) {
            sb.append(";expires=").append(jo.getString("expires"));
        }
        if (jo.has("domain")) {
            sb.append(";domain=").append(escape(jo.getString("domain")));
        }
        if (jo.has("path")) {
            sb.append(";path=").append(escape(jo.getString("path")));
        }
        if (jo.optBoolean("secure")) {
            sb.append(";secure");
        }
        return sb.toString();
    }

    /**
     * Convert {@code %}<i>hh</i> sequences to single characters, and convert plus to space.
     *
     * @param string A string that may contain {@code +} &nbsp;<small>(plus)</small> and <code>%</code><i>hh</i>
     *               sequences.
     * @return The unescaped string.
     */
    public static String unescape(final String string) {
        final int length = string.length();
        final StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; ++i) {
            char c = string.charAt(i);
            if (c == '+') {
                c = ' ';
            } else if ((c == '%') && ((i + 2) < length)) {
                final int d = JSONTokener.dehexchar(string.charAt(i + 1));
                final int e = JSONTokener.dehexchar(string.charAt(i + 2));
                if ((d >= 0) && (e >= 0)) {
                    c = (char) ((d * 16) + e);
                    i += 2;
                }
            }
            sb.append(c);
        }
        return sb.toString();
    }
}
