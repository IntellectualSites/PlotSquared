package com.github.intellectualsites.plotsquared.plot.util.world;

import com.github.intellectualsites.plotsquared.plot.util.MathMan;
import com.sk89q.worldedit.world.item.ItemType;
import com.sk89q.worldedit.world.item.ItemTypes;
import com.sk89q.worldedit.world.registry.LegacyMapper;

import java.util.Locale;

public final class ItemUtil {
    private ItemUtil(){}

    public static ItemType get(String input) {
        if (input == null || input.isEmpty()) {
            return ItemTypes.AIR;
        }
        input = input.toLowerCase(Locale.ROOT);
        if (Character.isDigit(input.charAt(0))) {
            String[] split = input.split(":");
            if (MathMan.isInteger(split[0])) {
                if (split.length == 2) {
                    if (MathMan.isInteger(split[1])) {
                        return LegacyMapper.getInstance().getItemFromLegacy(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
                    }
                } else {
                    return LegacyMapper.getInstance().getItemFromLegacy(Integer.parseInt(split[0]));
                }
            }
        }
        if (!input.split("\\[", 2)[0].contains(":")) input = "minecraft:" + input;
        return ItemTypes.get(input);
    }

    public static final ItemType[] parse(String commaDelimited) {
        String[] split = commaDelimited.split(",(?![^\\(\\[]*[\\]\\)])");
        ItemType[] result = new ItemType[split.length];
        for (int i = 0; i < split.length; i++) {
            result[i] = get(split[i]);
        }
        return result;
    }
}
