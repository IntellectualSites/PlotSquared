package com.plotsquared.bukkit.listeners;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

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

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.plotsquared.bukkit.util.BukkitUtil;
import com.plotsquared.listener.PlotListener;

public class PlayerEvents_1_8 extends PlotListener implements Listener {
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(final InventoryClickEvent event) {
        if (!event.isLeftClick() || (event.getAction() != InventoryAction.PLACE_ALL) || event.isShiftClick()) {
            return;
        }
        final HumanEntity entity = event.getWhoClicked();
        if (!(entity instanceof Player) || !PS.get().isPlotWorld(entity.getWorld().getName())) {
            return;
        }
        final Player player = (Player) entity;
        final PlayerInventory inv = player.getInventory();
        final int slot = inv.getHeldItemSlot();
        if ((slot != event.getSlot()) || (slot > 8) || !event.getEventName().equals("InventoryCreativeEvent")) {
            return;
        }
        final ItemStack current = inv.getItemInHand();
        final ItemStack newItem = event.getCursor();
        final ItemMeta newMeta = newItem.getItemMeta();
        final ItemMeta oldMeta = newItem.getItemMeta();
        String newLore = "";
        if (newMeta != null) {
            final List<String> lore = newMeta.getLore();
            if (lore != null) {
                newLore = lore.toString();
            }
        }
        String oldLore = "";
        if (oldMeta != null) {
            final List<String> lore = oldMeta.getLore();
            if (lore != null) {
                oldLore = lore.toString();
            }
        }
        if (!newLore.equals("[(+NBT)]") || (current.equals(newItem) && newLore.equals(oldLore))) {
            return;
        }
        final HashSet<Byte> blocks = null;
        final Block block = player.getTargetBlock(blocks, 7);
        final BlockState state = block.getState();
        if (state == null) {
            return;
        }
        if (state.getType() != newItem.getType()) {
            return;
        }
        final Location l = BukkitUtil.getLocation(state.getLocation());
        final Plot plot = MainUtil.getPlotAbs(l);
        final PlotPlayer pp = BukkitUtil.getPlayer(player);
        boolean cancelled = false;
        if (plot == null) {
            if (!MainUtil.isPlotArea(l)) {
                return;
            }
            if (!Permissions.hasPermission(pp, "plots.admin.interact.road")) {
                MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, "plots.admin.interact.road");
                cancelled = true;
            }
        } else {
            if (!plot.hasOwner()) {
                if (!Permissions.hasPermission(pp, "plots.admin.interact.unowned")) {
                    MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, "plots.admin.interact.unowned");
                    cancelled = true;
                }
            } else {
                final UUID uuid = pp.getUUID();
                if (!plot.isAdded(uuid)) {
                    if (!Permissions.hasPermission(pp, "plots.admin.interact.other")) {
                        MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, "plots.admin.interact.other");
                        cancelled = true;
                    }
                }
            }
        }
        if (cancelled) {
            if ((current.getTypeId() == newItem.getTypeId()) && (current.getDurability() == newItem.getDurability())) {
                event.setCursor(new ItemStack(newItem.getTypeId(), newItem.getAmount(), newItem.getDurability()));
                event.setCancelled(true);
                return;
            }
            event.setCursor(new ItemStack(newItem.getTypeId(), newItem.getAmount(), newItem.getDurability()));
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInteract(final PlayerInteractAtEntityEvent e) {
        final Entity entity = e.getRightClicked();
        if (!(entity instanceof ArmorStand)) {
            return;
        }
        final Location l = BukkitUtil.getLocation(e.getRightClicked().getLocation());
        final String world = l.getWorld();
        if (!PS.get().isPlotWorld(world)) {
            return;
        }
        final Plot plot = MainUtil.getPlotAbs(l);
        final PlotPlayer pp = BukkitUtil.getPlayer(e.getPlayer());
        if (plot == null) {
            if (!MainUtil.isPlotArea(l)) {
                return;
            }
            if (!Permissions.hasPermission(pp, "plots.admin.interact.road")) {
                MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, "plots.admin.interact.road");
                e.setCancelled(true);
            }
        } else {
            if (!plot.hasOwner()) {
                if (!Permissions.hasPermission(pp, "plots.admin.interact.unowned")) {
                    MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, "plots.admin.interact.unowned");
                    e.setCancelled(true);
                }
            } else {
                final UUID uuid = pp.getUUID();
                if (!plot.isAdded(uuid)) {
                    if (!Permissions.hasPermission(pp, "plots.admin.interact.other")) {
                        MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, "plots.admin.interact.other");
                        e.setCancelled(true);
                    }
                }
            }
        }
    }
}
