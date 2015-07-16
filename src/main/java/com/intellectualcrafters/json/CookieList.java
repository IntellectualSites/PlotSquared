package com.intellectualcrafters.json;

import java.util.Iterator;

/**
 * Convert a web browser cookie list string to a JSONObject and back.
 *
 * @author JSON.org
 * @version 2014-05-03
 */
public class CookieList {
    /**
     * Convert a cookie list into a JSONObject. A cookie list is a sequence of name/value pairs. The names are separated
     * from the values by '='. The pairs are separated by ';'. The names and the values will be unescaped, possibly
     * converting '+' and '%' sequences.
     * 
     * To add a cookie to a cooklist, cookielistJSONObject.put(cookieJSONObject.getString("name"),
     * cookieJSONObject.getString("value"));
     *
     * @param string A cookie list string
     *
     * @return A JSONObject
     *
     * @throws JSONException
     */
    public static JSONObject toJSONObject(final String string) throws JSONException {
        final JSONObject jo = new JSONObject();
        final JSONTokener x = new JSONTokener(string);
        while (x.more()) {
            final String name = Cookie.unescape(x.nextTo('='));
            x.next('=');
            jo.put(name, Cookie.unescape(x.nextTo(';')));
            x.next();
        }
        return jo;
    }

    /**
     * Convert a JSONObject into a cookie list. A cookie list is a sequence of name/value pairs. The names are separated
     * from the values by '='. The pairs are separated by ';'. The characters '%', '+', '=', and ';' in the names and
     * values are replaced by "%hh".
     *
     * @param jo A JSONObject
     *
     * @return A cookie list string
     *
     * @throws JSONException
     */
    public static String toString(final JSONObject jo) throws JSONException {
        boolean b = false;
        final Iterator<String> keys = jo.keys();
        String string;
        final StringBuilder sb = new StringBuilder();
        while (keys.hasNext()) {
            string = keys.next();
            if (!jo.isNull(string)) {
                if (b) {
                    sb.append(';');
                }
                sb.append(Cookie.escape(string));
                sb.append("=");
                sb.append(Cookie.escape(jo.getString(string)));
                b = true;
            }
        }
        return sb.toString();
    }
}
