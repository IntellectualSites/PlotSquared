package com.plotsquared.plot.flags.implementations;

import com.plotsquared.config.Captions;
import com.plotsquared.plot.flags.FlagParseException;
import com.plotsquared.plot.flags.PlotFlag;
import com.plotsquared.util.ItemUtil;
import com.sk89q.worldedit.world.item.ItemType;
import com.sk89q.worldedit.world.item.ItemTypes;
import org.jetbrains.annotations.NotNull;

public class MusicFlag extends PlotFlag<ItemType, MusicFlag> {

    public static final MusicFlag MUSIC_FLAG_NONE = new MusicFlag(ItemTypes.AIR);

    /**
     * Construct a new flag instance.
     *
     * @param value Flag value
     */
    protected MusicFlag(ItemType value) {
        super(value, Captions.FLAG_CATEGORY_MUSIC, Captions.FLAG_DESCRIPTION_MUSIC);
    }

    @Override public MusicFlag parse(@NotNull String input) throws FlagParseException {
        if (!input.isEmpty() && !input.contains("music_disc_")) {
            input = "music_disc_" + input;
        }
        final ItemType itemType = ItemUtil.get(input);
        if (itemType != null && itemType.getId() != null &&
            (itemType == ItemTypes.AIR || itemType.getId().contains("music_disc_"))) {
            return new MusicFlag(ItemUtil.get(input));
        } else {
            throw new FlagParseException(this, input, Captions.FLAG_ERROR_MUSIC);
        }
    }

    @Override public MusicFlag merge(@NotNull ItemType newValue) {
        if (getValue().equals(ItemTypes.AIR)) {
            return new MusicFlag(newValue);
        } else if (newValue.equals(ItemTypes.AIR)) {
            return this;
        } else {
            return new MusicFlag(newValue);
        }
    }

    @Override public String toString() {
        return getValue().getId();
    }

    @Override public String getExample() {
        return "ward";
    }

    @Override protected MusicFlag flagOf(@NotNull ItemType value) {
        return new MusicFlag(value);
    }

}
