package com.intellectualcrafters.configuration.file;

import java.util.LinkedHashMap;
import java.util.Map;

import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.representer.Representer;

import com.intellectualcrafters.configuration.ConfigurationSection;
import com.intellectualcrafters.configuration.serialization.ConfigurationSerializable;
import com.intellectualcrafters.configuration.serialization.ConfigurationSerialization;

public class YamlRepresenter extends Representer
{

    public YamlRepresenter()
    {
        multiRepresenters.put(ConfigurationSection.class, new RepresentConfigurationSection());
        multiRepresenters.put(ConfigurationSerializable.class, new RepresentConfigurationSerializable());
    }

    private class RepresentConfigurationSection extends RepresentMap
    {
        @Override
        public Node representData(final Object data)
        {
            return super.representData(((ConfigurationSection) data).getValues(false));
        }
    }

    private class RepresentConfigurationSerializable extends RepresentMap
    {
        @Override
        public Node representData(final Object data)
        {
            final ConfigurationSerializable serializable = (ConfigurationSerializable) data;
            final Map<String, Object> values = new LinkedHashMap<String, Object>();
            values.put(ConfigurationSerialization.SERIALIZED_TYPE_KEY, ConfigurationSerialization.getAlias(serializable.getClass()));
            values.putAll(serializable.serialize());

            return super.representData(values);
        }
    }
}
