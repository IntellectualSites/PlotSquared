package com.plotsquared.listener;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
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
import com.sk89q.worldedit.extent.reorder.MultiStageReorder;
import com.sk89q.worldedit.extent.world.FastModeExtent;
import com.sk89q.worldedit.util.eventbus.EventHandler.Priority;
import com.sk89q.worldedit.util.eventbus.Subscribe;
import com.sk89q.worldedit.world.World;

import java.lang.reflect.Field;
import java.util.HashSet;

public class WESubscriber {
    
    @Subscribe(priority = Priority.VERY_EARLY)
    public void onEditSession(final EditSessionEvent event) {
        final WorldEdit worldedit = PS.get().worldedit;
        if (worldedit == null) {
            WorldEdit.getInstance().getEventBus().unregister(this);
            return;
        }
        final World worldObj = event.getWorld();
        final String world = worldObj.getName();
        final Actor actor = event.getActor();
        if (actor != null && actor.isPlayer()) {
            final String name = actor.getName();
            final PlotPlayer pp = PlotPlayer.wrap(name);
            if (pp != null && pp.getAttribute("worldedit")) {
                return;
            }
            final HashSet<RegionWrapper> mask = WEManager.getMask(pp);
            if (mask.isEmpty()) {
                if (Permissions.hasPermission(pp, "plots.worldedit.bypass")) {
                    MainUtil.sendMessage(pp, C.WORLDEDIT_BYPASS);
                }
                if (PS.get().hasPlotArea(world)) {
                    event.setExtent(new NullExtent());
                }
                return;
            }
            if (Settings.CHUNK_PROCESSOR) {
                if (Settings.EXPERIMENTAL_FAST_ASYNC_WORLDEDIT) {
                    try {
                        final LocalSession session = worldedit.getSessionManager().findByName(name);
                        boolean hasMask = session.getMask() != null;
                        final Player objPlayer = (Player) actor;
                        final int item = objPlayer.getItemInHand();
                        if (!hasMask) {
                            try {
                                final Tool tool = session.getTool(item);
                                if (tool instanceof BrushTool) {
                                    hasMask = ((BrushTool) tool).getMask() != null;
                                }
                            } catch (final Exception e) {
                            }
                        }
                        AbstractDelegateExtent extent = (AbstractDelegateExtent) event.getExtent();
                        ChangeSetExtent history = null;
                        MultiStageReorder reorder = null;
                        MaskingExtent maskextent = null;
                        final boolean fast = session.hasFastMode();
                        while (extent.getExtent() != null && extent.getExtent() instanceof AbstractDelegateExtent) {
                            final AbstractDelegateExtent tmp = (AbstractDelegateExtent) extent.getExtent();
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
                        final int max = event.getMaxBlocks();
                        final Field field = AbstractDelegateExtent.class.getDeclaredField("extent");
                        field.setAccessible(true);
                        if (history == null) {
                            final ExtentWrapper wrapper = new ExtentWrapper(event.getExtent());
                            event.setExtent(wrapper);
                            field.set(extent, new ProcessedWEExtent(world, mask, max, new FastModeExtent(worldObj, true), wrapper));
                        } else {
                            if (fast) {
                                event.setExtent(new ExtentWrapper(extent));
                            } else {
                                ExtentWrapper wrapper;
                                if (maskextent != null) {
                                    wrapper = new ExtentWrapper(maskextent);
                                    field.set(maskextent, history);
                                    event.setExtent(wrapper);
                                } else {
                                    wrapper = new ExtentWrapper(history);
                                    event.setExtent(wrapper);
                                }
                                field.set(history, reorder);
                                field.set(reorder, new ProcessedWEExtent(world, mask, max, new FastModeExtent(worldObj, true), wrapper));
                            }
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
