package com.intellectualcrafters.json;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;

/**
 * Converts a Property file data into JSONObject and back.
 *
 * @author JSON.org
 * @version 2014-05-03
 */
public class Property {
    /**
     * Converts a property file object into a JSONObject. The property file object is a table of name value pairs.
     *
     * @param properties java.util.Properties
     *
     * @return JSONObject
     *
     * @throws JSONException
     */
    public static JSONObject toJSONObject(final java.util.Properties properties) throws JSONException {
        final JSONObject jo = new JSONObject();
        if ((properties != null) && !properties.isEmpty()) {
            final Enumeration enumProperties = properties.propertyNames();
            while (enumProperties.hasMoreElements()) {
                final String name = (String) enumProperties.nextElement();
                jo.put(name, properties.getProperty(name));
            }
        }
        return jo;
    }
    
    /**
     * Converts the JSONObject into a property file object.
     *
     * @param jo JSONObject
     *
     * @return java.util.Properties
     *
     * @throws JSONException
     */
    public static Properties toProperties(final JSONObject jo) throws JSONException {
        final Properties properties = new Properties();
        if (jo != null) {
            final Iterator<String> keys = jo.keys();
            while (keys.hasNext()) {
                final String name = keys.next();
                properties.put(name, jo.getString(name));
            }
        }
        return properties;
    }
}
