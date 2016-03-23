package com.plotsquared.bukkit.chat;

import com.google.gson.stream.JsonWriter;
import com.intellectualcrafters.configuration.serialization.ConfigurationSerializable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a JSON string value.
 * Writes by this object will not write name values nor begin/end objects in the JSON stream.
 * All writes merely write the represented string value.
 */
final class JsonString implements JsonRepresentedObject, ConfigurationSerializable {

    private final String _value;

    public JsonString(CharSequence value) {
        this._value = value == null ? null : value.toString();
    }

    public static JsonString deserialize(Map<String, Object> map) {
        return new JsonString(map.get("stringValue").toString());
    }

    @Override
    public void writeJson(JsonWriter writer) throws IOException {
        writer.value(getValue());
    }

    public String getValue() {
        return this._value;
    }

    @Override
    public Map<String, Object> serialize() {
        HashMap<String, Object> theSingleValue = new HashMap<>();
        theSingleValue.put("stringValue", this._value);
        return theSingleValue;
    }

    @Override
    public String toString() {
        return this._value;
    }
}
