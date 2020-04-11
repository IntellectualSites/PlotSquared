package com.plotsquared.configuration.file;

import com.plotsquared.configuration.ConfigurationSection;
import com.plotsquared.configuration.serialization.ConfigurationSerializable;
import com.plotsquared.configuration.serialization.ConfigurationSerialization;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.representer.Representer;

import java.util.LinkedHashMap;
import java.util.Map;

class YamlRepresenter extends Representer {

    YamlRepresenter() {
        this.multiRepresenters.put(ConfigurationSection.class, new RepresentConfigurationSection());
        this.multiRepresenters
            .put(ConfigurationSerializable.class, new RepresentConfigurationSerializable());
    }

    private class RepresentConfigurationSection extends RepresentMap {

        @Override public Node representData(Object data) {
            return super.representData(((ConfigurationSection) data).getValues(false));
        }
    }


    private class RepresentConfigurationSerializable extends RepresentMap {

        @Override public Node representData(Object data) {
            ConfigurationSerializable serializable = (ConfigurationSerializable) data;
            Map<String, Object> values = new LinkedHashMap<>();
            values.put(ConfigurationSerialization.SERIALIZED_TYPE_KEY,
                ConfigurationSerialization.getAlias(serializable.getClass()));
            values.putAll(serializable.serialize());

            return super.representData(values);
        }
    }
}
