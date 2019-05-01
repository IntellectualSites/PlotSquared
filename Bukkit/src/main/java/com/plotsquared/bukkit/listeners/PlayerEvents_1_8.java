package com.plotsquared.bukkit.listeners;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.flag.Flags;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.plotsquared.bukkit.util.BukkitUtil;
import com.plotsquared.listener.PlotListener;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import org.bukkit.Material;
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

public class PlayerEvents_1_8 extends PlotListener implements Listener {
    
    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.isLeftClick() || (event.getAction() != InventoryAction.PLACE_ALL) || event.isShiftClick()) {
            return;
        }
        HumanEntity entity = event.getWhoClicked();
        if (!(entity instanceof Player) || !PS.get().hasPlotArea(entity.getWorld().getName())) {
            return;
        }
        Player player = (Player) entity;
        PlayerInventory inv = player.getInventory();
        int slot = inv.getHeldItemSlot();
        if ((slot > 8) || !event.getEventName().equals("InventoryCreativeEvent")) {
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
        if (!"[(+NBT)]".equals(newLore) || (current.equals(newItem) && newLore.equals(oldLore))) {
            switch (newItem.getType()) {
                case BANNER:
                case SKULL_ITEM:
                    if (newMeta != null) break;
                default:
                    return;
            }
        }

        HashSet<Material> blocks = null;
        Block block = player.getTargetBlock(blocks, 7);
        BlockState state = block.getState();
        if (state == null) {
            return;
        }
        Material stateType = state.getType();
        Material itemType = newItem.getType();
        if (stateType != itemType) {
            switch (stateType) {
                case STANDING_BANNER:
                case WALL_BANNER:
                    if (itemType == Material.BANNER) break;
                case SKULL:
                    if (itemType == Material.SKULL_ITEM) break;
                default:
                    return;
            }
        }
        Location l = BukkitUtil.getLocation(state.getLocation());
        PlotArea area = l.getPlotArea();
        if (area == null) {
            return;
        }
        Plot plot = area.getPlotAbs(l);
        PlotPlayer pp = BukkitUtil.getPlayer(player);
        boolean cancelled = false;
        if (plot == null) {
            if (!Permissions.hasPermission(pp, "plots.admin.interact.road")) {
                MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, "plots.admin.interact.road");
                cancelled = true;
            }
        } else if (!plot.hasOwner()) {
            if (!Permissions.hasPermission(pp, "plots.admin.interact.unowned")) {
                MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, "plots.admin.interact.unowned");
                cancelled = true;
            }
        } else {
            UUID uuid = pp.getUUID();
            if (!plot.isAdded(uuid)) {
                if (!Permissions.hasPermission(pp, "plots.admin.interact.other")) {
                    MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, "plots.admin.interact.other");
                    cancelled = true;
                }
            }
        }
        if (cancelled) {
            if ((current.getType() == newItem.getType()) && (current.getDurability() == newItem.getDurability())) {
                event.setCursor(new ItemStack(newItem.getType(), newItem.getAmount(), newItem.getDurability()));
                event.setCancelled(true);
                return;
            }
            event.setCursor(new ItemStack(newItem.getType(), newItem.getAmount(), newItem.getDurability()));
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInteract(PlayerInteractAtEntityEvent e) {
        Entity entity = e.getRightClicked();
        if (!(entity instanceof ArmorStand)) {
            return;
        }
        Location l = BukkitUtil.getLocation(e.getRightClicked().getLocation());
        PlotArea area = l.getPlotArea();
        if (area == null) {
            return;
        }

        EntityPortal_1_7_9.test(entity);

        Plot plot = area.getPlotAbs(l);
        PlotPlayer pp = BukkitUtil.getPlayer(e.getPlayer());
        if (plot == null) {
            if (!Permissions.hasPermission(pp, "plots.admin.interact.road")) {
                MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, "plots.admin.interact.road");
                e.setCancelled(true);
            }
        } else {
            if (Settings.Done.RESTRICT_BUILDING && plot.hasFlag(Flags.DONE)) {
                if (!Permissions.hasPermission(pp, C.PERMISSION_ADMIN_BUILD_OTHER)) {
                    MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_BUILD_OTHER);
                    e.setCancelled(true);
                    return;
                }
            }

            if (!plot.hasOwner()) {
                if (!Permissions.hasPermission(pp, "plots.admin.interact.unowned")) {
                    MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, "plots.admin.interact.unowned");
                    e.setCancelled(true);
                }
            } else {
                UUID uuid = pp.getUUID();
                if (!plot.isAdded(uuid)) {
                    if (Flags.MISC_INTERACT.isTrue(plot)) {
                        return;
                    }
                    if (!Permissions.hasPermission(pp, "plots.admin.interact.other")) {
                        MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, "plots.admin.interact.other");
                        e.setCancelled(true);
                    }
                }
            }
        }
    }
}
