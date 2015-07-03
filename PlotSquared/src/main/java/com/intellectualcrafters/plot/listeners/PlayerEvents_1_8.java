package com.intellectualcrafters.plot.listeners;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.bukkit.BukkitUtil;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class PlayerEvents_1_8 extends PlotListener implements Listener {
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.isLeftClick() || event.getAction() != InventoryAction.PLACE_ALL || event.isShiftClick()) {
            return;
        }
        HumanEntity entity = event.getWhoClicked();
        if (!(entity instanceof Player) || !PS.get().isPlotWorld(entity.getWorld().getName())) {
            return;
        }
        Player player = (Player) entity;
        PlayerInventory inv = player.getInventory();
        int slot = inv.getHeldItemSlot();
        if (slot != event.getSlot() || slot > 8 || !event.getEventName().equals("InventoryCreativeEvent")) {
            return;
        }
        ItemStack current = inv.getItemInHand();
        ItemStack newItem = event.getCursor();
        ItemMeta newMeta = newItem.getItemMeta();
        ItemMeta oldMeta = newItem.getItemMeta();
        String newLore = "";
        if (newMeta != null) {
            List<String> lore = newMeta.getLore();
            if (lore != null) {
                newLore = lore.toString();
            }
        }
        String oldLore = "";
        if (oldMeta != null) {
            List<String> lore = oldMeta.getLore();
            if (lore != null) {
                oldLore = lore.toString();
            }
        }
        if (!newLore.equals("[(+NBT)]") || (current.equals(newItem) && newLore.equals(oldLore))) {
            return;
        }
        HashSet<Byte> blocks = null;
        Block block = player.getTargetBlock(blocks, 7);
        BlockState state = block.getState();
        if (state == null) {
            return;
        }
        if (state.getType() != newItem.getType()) {
            return;
        }
        final Location l = BukkitUtil.getLocation(state.getLocation());
        Plot plot = MainUtil.getPlot(l);
        PlotPlayer pp = BukkitUtil.getPlayer(player);
        boolean cancelled = false;
        if (plot == null) {
            if (!MainUtil.isPlotArea(l)) {
                return;
            }
            if (!Permissions.hasPermission(pp, "plots.admin.interact.road")) {
                MainUtil.sendMessage(pp, C.NO_PERMISSION, "plots.admin.interact.road");
                cancelled = true;
            }
        }
        else {
            if (!plot.hasOwner()) {
                if (!Permissions.hasPermission(pp, "plots.admin.interact.unowned")) {
                    MainUtil.sendMessage(pp, C.NO_PERMISSION, "plots.admin.interact.unowned");
                    cancelled = true;
                }
            }
            else {
                final UUID uuid = pp.getUUID();
                if (!plot.isAdded(uuid)) {
                    if (!Permissions.hasPermission(pp, "plots.admin.interact.other")) {
                        MainUtil.sendMessage(pp, C.NO_PERMISSION, "plots.admin.interact.other");
                        cancelled = true;
                    }
                }
            }
        }
        if (cancelled) {
            if (current.getTypeId() == newItem.getTypeId() && current.getDurability() == newItem.getDurability()) {
                event.setCursor(new ItemStack(newItem.getTypeId(), newItem.getAmount(), newItem.getDurability()));
                event.setCancelled(true);
                return;
            }
            event.setCursor(new ItemStack(newItem.getTypeId(), newItem.getAmount(), newItem.getDurability()));
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInteract(final PlayerInteractAtEntityEvent e) {
        Entity entity = e.getRightClicked();
        if (!(entity instanceof ArmorStand)) {
            return;
        }
        final Location l = BukkitUtil.getLocation(e.getRightClicked().getLocation());
        String world = l.getWorld();
        if (!PS.get().isPlotWorld(world)) {
            return;
        }
        Plot plot = MainUtil.getPlot(l);
        PlotPlayer pp = BukkitUtil.getPlayer(e.getPlayer());
        if (plot == null) {
            if (!MainUtil.isPlotArea(l)) {
                return;
            }
            if (!Permissions.hasPermission(pp, "plots.admin.interact.road")) {
                MainUtil.sendMessage(pp, C.NO_PERMISSION, "plots.admin.interact.road");
                e.setCancelled(true);
            }
        }
        else {
            if (!plot.hasOwner()) {
                if (!Permissions.hasPermission(pp, "plots.admin.interact.unowned")) {
                    MainUtil.sendMessage(pp, C.NO_PERMISSION, "plots.admin.interact.unowned");
                    e.setCancelled(true);
                }
            }
            else {
                final UUID uuid = pp.getUUID();
                if (!plot.isAdded(uuid)) {
                    if (!Permissions.hasPermission(pp, "plots.admin.interact.other")) {
                        MainUtil.sendMessage(pp, C.NO_PERMISSION, "plots.admin.interact.other");
                        e.setCancelled(true);
                    }
                }
            }
        }
    }
}
