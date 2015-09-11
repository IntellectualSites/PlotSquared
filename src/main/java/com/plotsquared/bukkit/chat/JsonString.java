package com.plotsquared.bukkit.chat;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.stream.JsonWriter;
import com.intellectualcrafters.configuration.serialization.ConfigurationSerializable;

/**
 * Represents a JSON string value.
 * Writes by this object will not write name values nor begin/end objects in the JSON stream.
 * All writes merely write the represented string value.
 */
final class JsonString implements JsonRepresentedObject, ConfigurationSerializable
{

    private final String _value;

    public JsonString(final CharSequence value)
    {
        _value = value == null ? null : value.toString();
    }

    @Override
    public void writeJson(final JsonWriter writer) throws IOException
    {
        writer.value(getValue());
    }

    public String getValue()
    {
        return _value;
    }

    @Override
    public Map<String, Object> serialize()
    {
        final HashMap<String, Object> theSingleValue = new HashMap<String, Object>();
        theSingleValue.put("stringValue", _value);
        return theSingleValue;
    }

    public static JsonString deserialize(final Map<String, Object> map)
    {
        return new JsonString(map.get("stringValue").toString());
    }

    @Override
    public String toString()
    {
        return _value;
    }
}
