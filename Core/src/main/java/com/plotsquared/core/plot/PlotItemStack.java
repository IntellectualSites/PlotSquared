package com.plotsquared.core.plot;

import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.item.ItemType;
import com.sk89q.worldedit.world.item.ItemTypes;
import com.sk89q.worldedit.world.registry.LegacyMapper;
import lombok.Getter;

public class PlotItemStack {

    public final int amount;
    public final String name;
    public final String[] lore;
    @Getter private final ItemType type;

    /**
     * @param id     Legacy numerical item ID
     * @param data   Legacy numerical item data
     * @param amount Amount of items in the stack
     * @param name   The display name of the item stack
     * @param lore   The item stack lore
     * @deprecated Use {@link PlotItemStack(String, int, String, String...)}
     */
    @Deprecated public PlotItemStack(final int id, final short data, final int amount,
        final String name, final String... lore) {

        this.amount = amount;
        this.name = name;
        this.lore = lore;
        this.type = LegacyMapper.getInstance().getItemFromLegacy(id, data);
    }

    /**
     * @param id     String ID
     * @param amount Amount of items in the stack
     * @param name   The display name of the item stack
     * @param lore   The item stack lore
     */
    public PlotItemStack(final String id, final int amount, final String name,
        final String... lore) {
        this.type = ItemTypes.get(id);
        this.amount = amount;
        this.name = name;
        this.lore = lore;
    }

    public BlockState getBlockState() {
        return type.getBlockType().getDefaultState();
    }
}
