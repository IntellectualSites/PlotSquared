/*
 *       _____  _       _    _____                                _
 *      |  __ \| |     | |  / ____|                              | |
 *      | |__) | | ___ | |_| (___   __ _ _   _  __ _ _ __ ___  __| |
 *      |  ___/| |/ _ \| __|\___ \ / _` | | | |/ _` | '__/ _ \/ _` |
 *      | |    | | (_) | |_ ____) | (_| | |_| | (_| | | |  __/ (_| |
 *      |_|    |_|\___/ \__|_____/ \__, |\__,_|\__,_|_|  \___|\__,_|
 *                                    | |
 *                                    |_|
 *            PlotSquared plot management system for Minecraft
 *                  Copyright (C) 2020 IntellectualSites
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.intellectualsites.plotsquared.plot.object.worlds;

import com.github.intellectualsites.plotsquared.configuration.ConfigurationSection;
import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.config.Configuration;
import com.github.intellectualsites.plotsquared.plot.config.ConfigurationNode;
import com.github.intellectualsites.plotsquared.plot.flags.FlagContainer;
import com.github.intellectualsites.plotsquared.plot.generator.GridPlotWorld;
import com.github.intellectualsites.plotsquared.plot.object.Location;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotAreaType;
import com.github.intellectualsites.plotsquared.plot.object.PlotId;
import com.github.intellectualsites.plotsquared.plot.object.PlotLoc;
import com.github.intellectualsites.plotsquared.plot.object.PlotManager;
import com.github.intellectualsites.plotsquared.plot.object.PlotSettings;
import com.github.intellectualsites.plotsquared.plot.object.RunnableVal;
import com.github.intellectualsites.plotsquared.plot.object.SetupObject;
import com.github.intellectualsites.plotsquared.plot.util.SetupUtils;
import com.github.intellectualsites.plotsquared.plot.util.TaskManager;
import com.github.intellectualsites.plotsquared.plot.util.WorldUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class SinglePlotArea extends GridPlotWorld {

    public boolean VOID = false;

    public SinglePlotArea() {
        super("*", null, new SingleWorldGenerator(), null, null);
        this.setAllowSigns(false);
        this.setDefaultHome(new PlotLoc(Integer.MAX_VALUE, Integer.MAX_VALUE));
    }

    @NotNull @Override protected PlotManager createManager() {
        return new SinglePlotManager(this);
    }

    @Override public void loadConfiguration(ConfigurationSection config) {
        VOID = config.getBoolean("void", false);
    }

    @Override public void saveConfiguration(ConfigurationSection config) {
        super.saveConfiguration(config);
    }

    public void loadWorld(final PlotId id) {
        String worldName = id.getX() + "." + id.getY();
        if (WorldUtil.IMP.isWorld(worldName)) {
            return;
        }
        SetupObject setup = new SetupObject();
        setup.plotManager = "PlotSquared:single";
        setup.setupGenerator = "PlotSquared:single";
        setup.type = getType();
        setup.terrain = getTerrain();
        setup.step = new ConfigurationNode[0];
        setup.world = worldName;

        File container = PlotSquared.imp().getWorldContainer();
        File destination = new File(container, worldName);

        {// convert old
            File oldFile = new File(container, id.toCommaSeparatedString());
            if (oldFile.exists()) {
                oldFile.renameTo(destination);
            }
        }
        // Duplicate 0;0
        if (setup.type != PlotAreaType.NORMAL) {
            if (!destination.exists()) {
                File src = new File(container, "0.0");
                if (src.exists()) {
                    if (!destination.exists()) {
                        destination.mkdirs();
                    }
                    File levelDat = new File(src, "level.dat");
                    if (levelDat.exists()) {
                        try {
                            Files.copy(levelDat.toPath(),
                                new File(destination, levelDat.getName()).toPath());
                            File data = new File(src, "data");
                            if (data.exists()) {
                                File dataDest = new File(destination, "data");
                                dataDest.mkdirs();
                                for (File file : data.listFiles()) {
                                    Files.copy(file.toPath(),
                                        new File(dataDest, file.getName()).toPath());
                                }
                            }
                        } catch (IOException exception) {
                            exception.printStackTrace();
                        }
                    }
                }
            }
        }

        TaskManager.IMP.sync(new RunnableVal<Object>() {
            @Override public void run(Object value) {
                String worldName = id.getX() + "." + id.getY();
                if (WorldUtil.IMP.isWorld(worldName)) {
                    return;
                }

                SetupUtils.manager.setupWorld(setup);
            }
        });
        //        String worldName = plot.getWorldName();
        //        World world = Bukkit.getWorld(worldName);
        //        if (world != null) {
        //            return world;
        //        }
        //        WorldCreator wc = new WorldCreator(worldName);
        //        wc.generator("PlotSquared:single");
        //        wc.environment(World.Environment.NORMAL);
        //        wc.type(WorldType.FLAT);
        //        return AsyncWorld.create(wc);
    }



    @Override public ConfigurationNode[] getSettingNodes() {
        return new ConfigurationNode[] {
            new ConfigurationNode("void", this.VOID, "Void world", Configuration.BOOLEAN)};
    }

    @Nullable @Override public Plot getOwnedPlot(@NotNull final Location location) {
        PlotId pid = PlotId.fromStringOrNull(location.getWorld());
        Plot plot = pid == null ? null : this.plots.get(pid);
        return plot == null ? null : plot.getBasePlot(false);
    }

    @Nullable @Override public Plot getOwnedPlotAbs(@NotNull Location location) {
        PlotId pid = PlotId.fromStringOrNull(location.getWorld());
        return pid == null ? null : plots.get(pid);
    }

    @Nullable @Override public Plot getPlot(@NotNull final Location location) {
        PlotId pid = PlotId.fromStringOrNull(location.getWorld());
        return pid == null ? null : getPlot(pid);
    }

    @Nullable @Override public Plot getPlotAbs(@NotNull final Location location) {
        final PlotId pid = PlotId.fromStringOrNull(location.getWorld());
        return pid == null ? null : getPlotAbs(pid);
    }

    public boolean addPlot(@NotNull Plot plot) {
        plot = adapt(plot);
        return super.addPlot(plot);
    }

    @Override public boolean addPlotAbs(@NotNull Plot plot) {
        plot = adapt(plot);
        return super.addPlotAbs(plot);
    }

    @Override public boolean addPlotIfAbsent(@NotNull Plot plot) {
        plot = adapt(plot);
        return super.addPlotIfAbsent(plot);
    }

    protected Plot adapt(Plot p) {
        if (p instanceof SinglePlot) {
            return p;
        }
        PlotSettings s = p.getSettings();

        final FlagContainer oldContainer = p.getFlagContainer();
        p = new SinglePlot(p.getId(), p.getOwnerAbs(), p.getTrusted(), p.getMembers(), p.getDenied(),
            s.getAlias(), s.getPosition(), null, this, s.getMerged(), p.getTimestamp(), p.temp);
        p.getFlagContainer().addAll(oldContainer);

        return p;
    }

    @Nullable public Plot getPlotAbs(@NotNull final PlotId id) {
        Plot plot = getOwnedPlotAbs(id);
        if (plot == null) {
            return new SinglePlot(this, id);
        }
        return plot;
    }

    @Nullable public Plot getPlot(@NotNull PlotId id) {
        // TODO
        Plot plot = getOwnedPlotAbs(id);
        if (plot == null) {
            return new SinglePlot(this, id);
        }
        return plot.getBasePlot(false);
    }
}
