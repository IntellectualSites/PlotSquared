package com.plotsquared.listener;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.RegionWrapper;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.command.tool.BrushTool;
import com.sk89q.worldedit.command.tool.Tool;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.event.extent.EditSessionEvent;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.ChangeSetExtent;
import com.sk89q.worldedit.extent.MaskingExtent;
import com.sk89q.worldedit.extent.NullExtent;
import com.sk89q.worldedit.extent.reorder.MultiStageReorder;
import com.sk89q.worldedit.extent.world.FastModeExtent;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.eventbus.EventHandler.Priority;
import com.sk89q.worldedit.util.eventbus.Subscribe;
import com.sk89q.worldedit.world.World;

import java.lang.reflect.Field;
import java.util.HashSet;

public class WESubscriber {

    @Subscribe(priority = Priority.VERY_EARLY)
    public void onEditSession(EditSessionEvent event) {
        WorldEdit worldedit = PS.get().worldedit;
        if (worldedit == null) {
            WorldEdit.getInstance().getEventBus().unregister(this);
            return;
        }
        World worldObj = event.getWorld();
        String world = worldObj.getName();
        Actor actor = event.getActor();
        if (actor != null && actor.isPlayer()) {
            String name = actor.getName();
            PlotPlayer pp = PlotPlayer.wrap(name);
            HashSet<RegionWrapper> mask;
            if (pp == null) {
                Player player = (Player) actor;
                Location loc = player.getLocation();
                com.intellectualcrafters.plot.object.Location pLoc =
                        new com.intellectualcrafters.plot.object.Location(player.getWorld().getName(), loc.getBlockX(), loc.getBlockX(),
                                loc.getBlockZ());
                Plot plot = pLoc.getPlot();
                if (plot == null) {
                    event.setExtent(new NullExtent());
                    return;
                }
                mask = plot.getRegions();
            } else if (pp.getAttribute("worldedit")) {
                return;
            } else {
                mask = WEManager.getMask(pp);
                if (mask.isEmpty()) {
                    if (Permissions.hasPermission(pp, "plots.worldedit.bypass")) {
                        MainUtil.sendMessage(pp, C.WORLDEDIT_BYPASS);
                    }
                    if (PS.get().hasPlotArea(world)) {
                        event.setExtent(new NullExtent());
                    }
                    return;
                }
            }
            if (Settings.CHUNK_PROCESSOR) {
                if (Settings.EXPERIMENTAL_FAST_ASYNC_WORLDEDIT) {
                    try {
                        LocalSession session = worldedit.getSessionManager().findByName(name);
                        boolean hasMask = session.getMask() != null;
                        Player objPlayer = (Player) actor;
                        int item = objPlayer.getItemInHand();
                        if (!hasMask) {
                            try {
                                Tool tool = session.getTool(item);
                                if (tool instanceof BrushTool) {
                                    hasMask = ((BrushTool) tool).getMask() != null;
                                }
                            } catch (Exception ignored) {}
                        }
                        AbstractDelegateExtent extent = (AbstractDelegateExtent) event.getExtent();
                        ChangeSetExtent history = null;
                        MultiStageReorder reorder = null;
                        MaskingExtent maskextent = null;
                        boolean fast = session.hasFastMode();
                        while (extent.getExtent() != null && extent.getExtent() instanceof AbstractDelegateExtent) {
                            AbstractDelegateExtent tmp = (AbstractDelegateExtent) extent.getExtent();
                            if (tmp.getExtent() != null && tmp.getExtent() instanceof AbstractDelegateExtent) {
                                if (tmp instanceof ChangeSetExtent) {
                                    history = (ChangeSetExtent) tmp;
                                }
                                if (tmp instanceof MultiStageReorder) {
                                    reorder = (MultiStageReorder) tmp;
                                }
                                if (hasMask && tmp instanceof MaskingExtent) {
                                    maskextent = (MaskingExtent) tmp;
                                }
                                extent = tmp;
                            } else {
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
                        } else if (fast) {
                            event.setExtent(new ExtentWrapper(extent));
                        } else {
                            ExtentWrapper wrapper;
                            if (maskextent != null) {
                                wrapper = new ExtentWrapper(maskextent);
                                field.set(maskextent, history);
                            } else {
                                wrapper = new ExtentWrapper(history);
                            }
                            event.setExtent(wrapper);
                            field.set(history, reorder);
                            field.set(reorder, new ProcessedWEExtent(world, mask, max, new FastModeExtent(worldObj, true), wrapper));
                        }
                        return;
                    } catch (IllegalAccessException | SecurityException | NoSuchFieldException | IllegalArgumentException e) {
                        e.printStackTrace();
                    }
                }
                if (PS.get().hasPlotArea(world)) {
                    event.setExtent(new ProcessedWEExtent(world, mask, event.getMaxBlocks(), event.getExtent(), event.getExtent()));
                }
            } else if (PS.get().hasPlotArea(world)) {
                event.setExtent(new WEExtent(mask, event.getExtent()));
            }
        }
    }
}
