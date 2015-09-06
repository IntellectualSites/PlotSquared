package com.plotsquared.sponge.util;

import java.util.ArrayList;
import java.util.Locale;

import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.Inventories;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackBuilder;
import org.spongepowered.api.item.inventory.custom.CustomInventory;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.item.inventory.type.CarriedInventory;

import com.intellectualcrafters.plot.object.PlotInventory;
import com.intellectualcrafters.plot.object.PlotItemStack;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.InventoryUtil;
import com.plotsquared.sponge.SpongeMain;
import com.plotsquared.sponge.object.SpongePlayer;

public class SpongeInventoryUtil extends InventoryUtil {

    public ItemStackBuilder builder;

    public SpongeInventoryUtil() {
        this.builder = SpongeMain.THIS.getGame().getRegistry().createItemBuilder();
    }
    
    @Override
    public void open(PlotInventory inv) {
        // TODO Auto-generated method stub
        SpongePlayer sp = (SpongePlayer) inv.player;
        Player player = sp.player;
        
        CustomInventory inventory = Inventories.customInventoryBuilder().name(SpongeMain.THIS.getTranslation(inv.getTitle())).size(inv.size).build();
        PlotItemStack[] items = inv.getItems();
        for (int i = 0; i < inv.size * 9; i++) {
            PlotItemStack item = items[i];
            if (item != null) {
                inventory.set(new SlotIndex(i), getItem(item));
            }
        }
        inv.player.setMeta("inventory", inv);
        player.openInventory(inventory);
    }
    

    public ItemStack getItem(PlotItemStack item) {
        // FIXME item type, item data, item name, item lore
        return builder.itemType(ItemTypes.SPONGE).quantity(item.amount).build();
    }


    @Override
    public void close(PlotInventory inv) {
        if (!inv.isOpen()) {
            return;
        }
        inv.player.deleteMeta("inventory");
        SpongePlayer sp = (SpongePlayer) inv.player;
        sp.player.closeInventory();
    }

    @Override
    public void setItem(PlotInventory inv, int index, PlotItemStack item) {
        if (!inv.isOpen()) {
            return;
        }
        SpongePlayer sp = (SpongePlayer) inv.player;
        Player player = sp.player;
        Inventory inventory = player.getOpenInventory().get();
        throw new UnsupportedOperationException("NOT IMPLEMENTED YET");
        
    }
    
    public PlotItemStack getItem(ItemStack item) {
        if (item == null) {
            return null;
        }
        ItemType type = item.getItem();
        String id = type.getId();
        int amount = item.getQuantity();
        // TODO name / lore
        return new PlotItemStack(id, amount, null);
    }

    @Override
    public PlotItemStack[] getItems(PlotPlayer player) {
        SpongePlayer sp = (SpongePlayer) player;
        CarriedInventory<? extends Carrier> inv = sp.player.getInventory();
        ArrayList<PlotItemStack> list = new ArrayList<PlotItemStack>();
        
        throw new UnsupportedOperationException("NOT IMPLEMENTED YET");
        
//        return list.toArray();
    }

    @Override
    public boolean isOpen(PlotInventory inv) {
        if (!inv.isOpen()) {
            return false;
        }
        SpongePlayer sp = (SpongePlayer) inv.player;
        Player player = sp.player;
        if (player.isViewingInventory()) {
            CarriedInventory<? extends Carrier> inventory = player.getInventory();
            return inv.getTitle().equals(inventory.getName().getTranslation().get(Locale.ENGLISH));
        }
        return false;
    }
    
}
