/*
 * PlotSquared, a land and world management plugin for Minecraft.
 * Copyright (C) IntellectualSites <https://intellectualsites.com>
 * Copyright (C) IntellectualSites team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.plot.world;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.ConfigurationNode;
import com.plotsquared.core.configuration.ConfigurationSection;
import com.plotsquared.core.configuration.ConfigurationUtil;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.configuration.file.YamlConfiguration;
import com.plotsquared.core.generator.GridPlotWorld;
import com.plotsquared.core.generator.SingleWorldGenerator;
import com.plotsquared.core.inject.annotations.WorldConfig;
import com.plotsquared.core.listener.PlotListener;
import com.plotsquared.core.location.BlockLoc;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotAreaType;
import com.plotsquared.core.plot.PlotId;
import com.plotsquared.core.plot.PlotManager;
import com.plotsquared.core.plot.PlotSettings;
import com.plotsquared.core.plot.flag.FlagContainer;
import com.plotsquared.core.queue.GlobalBlockQueue;
import com.plotsquared.core.setup.PlotAreaBuilder;
import com.plotsquared.core.setup.SettingsNodesWrapper;
import com.plotsquared.core.util.EventDispatcher;
import com.plotsquared.core.util.task.TaskManager;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class SinglePlotArea extends GridPlotWorld {

    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    private final EventDispatcher eventDispatcher;
    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    private final PlotListener plotListener;
    public boolean VOID = false;

    public SinglePlotArea(
            final @NonNull PlotAreaManager plotAreaManager,
            final @NonNull EventDispatcher eventDispatcher,
            final @NonNull PlotListener plotListener,
            @WorldConfig final @NonNull YamlConfiguration worldConfiguration,
            final @NonNull GlobalBlockQueue globalBlockQueue
    ) {
        super("*", null, new SingleWorldGenerator(plotAreaManager), null, null,
                worldConfiguration, globalBlockQueue
        );
        this.eventDispatcher = eventDispatcher;
        this.plotListener = plotListener;
        this.setAllowSigns(false);
        this.setDefaultHome(new BlockLoc(Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE));
    }

    /**
     * Returns true if the given string matches the naming system used to identify single plot worlds
     * e.g. -1_5 represents plot id *;-1;5. "*" being the plot area name given to single plot world
     * {@link com.plotsquared.core.plot.PlotArea}.
     *
     * @since 6.1.4
     */
    public static boolean isSinglePlotWorld(String worldName) {
        int len = worldName.length();
        int separator = 0;
        for (int i = 0; i < len; i++) {
            switch (worldName.charAt(i)) {
                case '_':
                    separator++;
                    break;
                case '-':
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    break;
                default:
                    return false;
            }
        }
        return separator == 1;
    }

    @NonNull
    @Override
    protected PlotManager createManager() {
        return new SinglePlotManager(this);
    }

    @Override
    public void loadConfiguration(ConfigurationSection config) {
        VOID = config.getBoolean("void", false);
    }

    @Override
    public void saveConfiguration(ConfigurationSection config) {
        super.saveConfiguration(config);
    }

    public void loadWorld(final PlotId id) {
        String worldName = id.toUnderscoreSeparatedString();
        if (PlotSquared.platform().worldUtil().isWorld(worldName)) {
            return;
        }
        PlotAreaBuilder builder = PlotAreaBuilder.newBuilder()
                .plotManager("PlotSquared:single")
                .generatorName("PlotSquared:single")
                .plotAreaType(getType())
                .terrainType(getTerrain())
                .settingsNodesWrapper(new SettingsNodesWrapper(new ConfigurationNode[0], null))
                .worldName(worldName);

        File container = PlotSquared.platform().worldContainer();
        File destination = new File(container, worldName);

        {// convert old
            File oldFile = new File(container, id.toCommaSeparatedString());
            if (oldFile.exists()) {
                oldFile.renameTo(destination);
            } else {
                oldFile = new File(container, id.toSeparatedString("."));
                if (oldFile.exists()) {
                    oldFile.renameTo(destination);
                }
            }
        }
        // Duplicate 0;0
        if (builder.plotAreaType() != PlotAreaType.NORMAL) {
            if (!destination.exists()) {
                File src = new File(container, "0_0");
                if (src.exists()) {
                    if (!destination.exists()) {
                        destination.mkdirs();
                    }
                    File levelDat = new File(src, "level.dat");
                    if (levelDat.exists()) {
                        try {
                            Files.copy(
                                    levelDat.toPath(),
                                    new File(destination, levelDat.getName()).toPath()
                            );
                            File data = new File(src, "data");
                            if (data.exists()) {
                                File dataDest = new File(destination, "data");
                                dataDest.mkdirs();
                                for (File file : data.listFiles()) {
                                    Files.copy(
                                            file.toPath(),
                                            new File(dataDest, file.getName()).toPath()
                                    );
                                }
                            }
                        } catch (IOException exception) {
                            exception.printStackTrace();
                        }
                    }
                }
            }
        }

        try {
            TaskManager.getPlatformImplementation().sync(() -> {
                final String name = id.toUnderscoreSeparatedString();
                if (!PlotSquared.platform().worldUtil().isWorld(name)) {
                    PlotSquared.platform().setupUtils().setupWorld(builder);
                }
                return null;
            });
        } catch (final Exception e) {
            e.printStackTrace();
        }

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


    @Override
    public ConfigurationNode[] getSettingNodes() {
        return new ConfigurationNode[]{
                new ConfigurationNode(
                        "void",
                        this.VOID,
                        TranslatableCaption.of("setup.singleplotarea_void_world"),
                        ConfigurationUtil.BOOLEAN
                )};
    }

    @Nullable
    @Override
    public Plot getOwnedPlot(final @NonNull Location location) {
        PlotId pid = PlotId.fromStringOrNull(location.getWorldName());
        Plot plot = pid == null ? null : this.plots.get(pid);
        return plot == null ? null : plot.getBasePlot(false);
    }

    @Nullable
    @Override
    public Plot getOwnedPlotAbs(@NonNull Location location) {
        PlotId pid = PlotId.fromStringOrNull(location.getWorldName());
        return pid == null ? null : plots.get(pid);
    }

    @Nullable
    @Override
    public Plot getPlot(final @NonNull Location location) {
        PlotId pid = PlotId.fromStringOrNull(location.getWorldName());
        return pid == null ? null : getPlot(pid);
    }

    @Nullable
    @Override
    public Plot getPlotAbs(final @NonNull Location location) {
        final PlotId pid = PlotId.fromStringOrNull(location.getWorldName());
        return pid == null ? null : getPlotAbs(pid);
    }

    public boolean addPlot(@NonNull Plot plot) {
        plot = adapt(plot);
        return super.addPlot(plot);
    }

    @Override
    public boolean addPlotAbs(@NonNull Plot plot) {
        plot = adapt(plot);
        return super.addPlotAbs(plot);
    }

    @Override
    public boolean addPlotIfAbsent(@NonNull Plot plot) {
        plot = adapt(plot);
        return super.addPlotIfAbsent(plot);
    }

    @Override
    public boolean allowSigns() {
        return false; // do not create signs for single plots
    }

    @SuppressWarnings("deprecation")
    protected Plot adapt(Plot p) {
        if (p instanceof SinglePlot) {
            return p;
        }
        PlotSettings s = p.getSettings();

        final FlagContainer oldContainer = p.getFlagContainer();
        p = new SinglePlot(p.getId(), p.getOwnerAbs(), p.getTrusted(), p.getMembers(),
                p.getDenied(), s.getAlias(), s.getPosition(), null, this, s.getMerged(),
                p.getTimestamp(), p.temp
        );
        p.getFlagContainer().addAll(oldContainer);

        return p;
    }

    public @Nullable Plot getPlotAbs(final @NonNull PlotId id) {
        Plot plot = getOwnedPlotAbs(id);
        if (plot == null) {
            return new SinglePlot(this, id);
        }
        return plot;
    }

    public @Nullable Plot getPlot(@NonNull PlotId id) {
        // TODO
        Plot plot = getOwnedPlotAbs(id);
        if (plot == null) {
            return new SinglePlot(this, id);
        }
        return plot.getBasePlot(false);
    }

}
