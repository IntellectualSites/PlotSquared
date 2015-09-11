package com.plotsquared.bukkit.chat;

import java.io.IOException;

import com.google.gson.stream.JsonWriter;

/**
 * Represents an object that can be serialized to a JSON writer instance.
 */
interface JsonRepresentedObject
{

    /**
     * Writes the JSON representation of this object to the specified writer.
     * @param writer The JSON writer which will receive the object.
     * @throws IOException If an error occurs writing to the stream.
     */
    void writeJson(final JsonWriter writer) throws IOException;

}
