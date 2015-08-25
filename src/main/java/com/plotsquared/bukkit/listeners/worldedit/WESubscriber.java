package com.plotsquared.bukkit.listeners.worldedit;

import java.lang.reflect.Field;
import java.util.HashSet;

import org.bukkit.entity.Player;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.RegionWrapper;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.plotsquared.bukkit.BukkitMain;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.EditSession.Stage;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.event.extent.EditSessionEvent;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.ChangeSetExtent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.cache.LastAccessExtentCache;
import com.sk89q.worldedit.extent.inventory.BlockBagExtent;
import com.sk89q.worldedit.extent.reorder.MultiStageReorder;
import com.sk89q.worldedit.extent.validation.DataValidatorExtent;
import com.sk89q.worldedit.extent.world.BlockQuirkExtent;
import com.sk89q.worldedit.extent.world.ChunkLoadingExtent;
import com.sk89q.worldedit.extent.world.FastModeExtent;
import com.sk89q.worldedit.extent.world.SurvivalModeExtent;
import com.sk89q.worldedit.util.eventbus.EventHandler.Priority;
import com.sk89q.worldedit.util.eventbus.Subscribe;
import com.sk89q.worldedit.world.World;

public class WESubscriber {
    
    @Subscribe(priority=Priority.VERY_EARLY)
    public void onEditSession(EditSessionEvent event) {
        World worldObj = event.getWorld();
        String world = worldObj.getName();
        if (!PS.get().isPlotWorld(world)) {
            return;
        }
        Actor actor = event.getActor();
        if (actor != null && actor.isPlayer()) {
            String name = actor.getName();
            PlotPlayer pp = PlotPlayer.wrap(name);
            if (pp != null && pp.getAttribute("worldedit")) {
                return;
            }

            PlotPlayer player = UUIDHandler.getPlayer(actor.getName());
            HashSet<RegionWrapper> mask = WEManager.getMask(player);
            if (mask.size() == 0) {
                if (Permissions.hasPermission(player, "plots.worldedit.bypass")) {
                    MainUtil.sendMessage(player, C.WORLDEDIT_BYPASS);
                }
                event.setExtent(new NullExtent());
                return;
            }
            if (Settings.CHUNK_PROCESSOR) {
                if (Settings.EXPERIMENTAL_FAST_ASYNC_WORLDEDIT) {
                    try {
                        AbstractDelegateExtent extent = (AbstractDelegateExtent) event.getExtent();
                        ChangeSetExtent history = null;
                        MultiStageReorder reorder = null;
                        boolean fast = ((BukkitMain) PS.get().IMP).worldEdit.getWorldEdit().getSession(name).hasFastMode();
                        while (extent.getExtent() != null && extent.getExtent() instanceof AbstractDelegateExtent) {
                            AbstractDelegateExtent tmp = (AbstractDelegateExtent) extent.getExtent();
                            if (tmp.getExtent() != null && tmp.getExtent() instanceof AbstractDelegateExtent) {
                                if (tmp instanceof ChangeSetExtent) {
                                    history = (ChangeSetExtent) tmp;
                                }
                                if (tmp instanceof MultiStageReorder) {
                                    reorder = (MultiStageReorder) tmp;
                                }
                                extent = tmp;
                            }
                            else {
                                break;
                            }
                        }
                        int max = event.getMaxBlocks();
                        Field field = AbstractDelegateExtent.class.getDeclaredField("extent");
                        field.setAccessible(true);
                        if (history == null) {
                            ExtentWrapper wrapper = new ExtentWrapper(event.getExtent());
                            event.setExtent(wrapper);
                            field.set(extent, new ProcessedWEExtent(world, mask, max, new FastModeExtent(worldObj, true), wrapper));
                        }
                        else {
                            if (fast) {
                                event.setExtent(new ExtentWrapper(extent));
                            }
                            else {
                                ExtentWrapper wrapper = new ExtentWrapper(history);
                                event.setExtent(wrapper);
                                field.set(history, reorder);
                                field.set(reorder, new ProcessedWEExtent(world, mask, max, new FastModeExtent(worldObj, true), wrapper));
                            }
                        }
                        return;
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                event.setExtent(new ProcessedWEExtent(world, mask, event.getMaxBlocks(), event.getExtent(), event.getExtent()));
            }
            else {
                event.setExtent(new WEExtent(mask, event.getExtent()));
            }
        }
    }
}
