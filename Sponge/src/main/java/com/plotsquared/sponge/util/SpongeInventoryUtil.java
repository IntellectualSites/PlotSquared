package com.plotsquared.sponge.util;

import com.intellectualcrafters.plot.object.PlotInventory;
import com.intellectualcrafters.plot.object.PlotItemStack;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.InventoryUtil;
import com.plotsquared.sponge.SpongeMain;
import com.plotsquared.sponge.object.SpongePlayer;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.custom.CustomInventory;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.item.inventory.type.CarriedInventory;

import java.util.ArrayList;

public class SpongeInventoryUtil extends InventoryUtil {

    public ItemStack.Builder builder;

    public SpongeInventoryUtil() {
        builder = SpongeMain.THIS.getGame().getRegistry().createBuilder(ItemStack.Builder.class);
    }

    @Override
    public void open(final PlotInventory inv) {
        // TODO Auto-generated method stub
        final SpongePlayer sp = (SpongePlayer) inv.player;
        final Player player = sp.player;

        final CustomInventory inventory = SpongeMain.THIS.getGame().getRegistry().createBuilder(CustomInventory.Builder.class).name(SpongeUtil.getTranslation(inv.getTitle())).size(inv.size).build();
        final PlotItemStack[] items = inv.getItems();
        for (int i = 0; i < (inv.size * 9); i++) {
            final PlotItemStack item = items[i];
            if (item != null) {
                inventory.set(new SlotIndex(i), getItem(item));
            }
        }
        inv.player.setMeta("inventory", inv);
        player.openInventory(inventory, SpongeUtil.CAUSE);
    }

    public ItemStack getItem(final PlotItemStack item) {
        // FIXME item type, item data, item name, item lore
        return builder.itemType(ItemTypes.SPONGE).quantity(item.amount).build();
    }

    @Override
    public void close(final PlotInventory inv) {
        if (!inv.isOpen()) {
            return;
        }
        inv.player.deleteMeta("inventory");
        final SpongePlayer sp = (SpongePlayer) inv.player;
        sp.player.closeInventory(SpongeUtil.CAUSE);
    }

    @Override
    public void setItem(final PlotInventory inv, final int index, final PlotItemStack item) {
        if (!inv.isOpen()) {
            return;
        }
        final SpongePlayer sp = (SpongePlayer) inv.player;
        final Player player = sp.player;
        player.getOpenInventory().get();
        throw new UnsupportedOperationException("NOT IMPLEMENTED YET");

    }

    public PlotItemStack getItem(final ItemStack item) {
        if (item == null) {
            return null;
        }
        final ItemType type = item.getItem();
        final String id = type.getId();
        final int amount = item.getQuantity();
        // TODO name / lore
        return new PlotItemStack(id, amount, null);
    }

    @Override
    public PlotItemStack[] getItems(final PlotPlayer player) {
        final SpongePlayer sp = (SpongePlayer) player;
        sp.player.getInventory();
        new ArrayList<PlotItemStack>();

        throw new UnsupportedOperationException("NOT IMPLEMENTED YET");

        //        return list.toArray();
    }

    @Override
    public boolean isOpen(final PlotInventory inv) {
        if (!inv.isOpen()) {
            return false;
        }
        final SpongePlayer sp = (SpongePlayer) inv.player;
        final Player player = sp.player;
        if (player.isViewingInventory()) {
            final CarriedInventory<? extends Carrier> inventory = player.getInventory();
            return inv.getTitle().equals(inventory.getName().getId()); // TODO getId()
        }
        return false;
    }

}
