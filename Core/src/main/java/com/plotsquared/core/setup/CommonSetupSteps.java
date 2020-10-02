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
package com.plotsquared.core.setup;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.Caption;
import com.plotsquared.core.configuration.Captions;
import com.plotsquared.core.events.TeleportCause;
import com.plotsquared.core.generator.GeneratorWrapper;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotAreaTerrainType;
import com.plotsquared.core.plot.PlotAreaType;
import com.plotsquared.core.plot.PlotId;
import com.plotsquared.core.util.MainUtil;
import com.plotsquared.core.util.SetupUtils;
import com.plotsquared.core.util.StringMan;
import com.plotsquared.core.util.WorldUtil;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.plotsquared.core.util.MainUtil.sendMessage;

public enum CommonSetupSteps implements SetupStep {
    CHOOSE_GENERATOR(Captions.SETUP_INIT) {

        @Override public SetupStep handleInput(PlotPlayer<?> plotPlayer, PlotAreaBuilder builder, String arg) {
            if (!SetupUtils.generators.containsKey(arg)) {
                String prefix = "\n&8 - &7";
                sendMessage(plotPlayer, Captions.SETUP_WORLD_GENERATOR_ERROR + prefix + StringMan
                        .join(SetupUtils.generators.keySet(), prefix)
                        .replaceAll(PlotSquared.imp().getPluginName(),
                                "&2" + PlotSquared.imp().getPluginName()));
                return this; // invalid input -> same setup step
            }
            builder.generatorName(arg);
            return CommonSetupSteps.CHOOSE_PLOT_AREA_TYPE; // proceed with next step
        }

        @NotNull @Override public Collection<String> getSuggestions() {
            return Collections.unmodifiableSet(SetupUtils.generators.keySet());
        }

        @Nullable @Override public String getDefaultValue() {
            return PlotSquared.imp().getPluginName();
        }
    },
    CHOOSE_PLOT_AREA_TYPE(PlotAreaType.class, Captions.SETUP_WORLD_TYPE) {

        @Override public SetupStep handleInput(PlotPlayer<?> plotPlayer, PlotAreaBuilder builder, String arg) {
            boolean withNormal = SetupUtils.generators.get(builder.generatorName()).isFull();
            Optional<PlotAreaType> plotAreaType = PlotAreaType.fromString(arg);
            if (!plotAreaType.isPresent()) {
                MainUtil.sendMessage(plotPlayer, Captions.SETUP_WORLD_TYPE_ERROR);
                PlotAreaType.getDescriptionMap().forEach((type, caption) -> {
                    if (!withNormal && type == PlotAreaType.NORMAL) {
                        return; // skip
                    }
                    String color = type == PlotAreaType.NORMAL ? "&2" : "&7";
                    MainUtil.sendMessage(plotPlayer, "&8 - " + color + type
                            + " &8-&7 " + caption.getTranslated());
                });
                return this;
            }
            builder.plotAreaType(plotAreaType.get());
            GeneratorWrapper<?> gen = SetupUtils.generators.get(builder.generatorName());
            if (builder.plotAreaType() == PlotAreaType.NORMAL) {
                if (builder.settingsNodesWrapper() == null) {
                    builder.plotManager(builder.generatorName());
                    builder.settingsNodesWrapper(CommonSetupSteps.wrap(builder.plotManager()));
                    SetupUtils.generators.get(builder.plotManager()).getPlotGenerator()
                            .processAreaSetup(builder);
                }
                return builder.settingsNodesWrapper().getFirstStep();
            } else {
                if (gen.isFull()) {
                    builder.plotManager(builder.generatorName());
                    builder.generatorName(null);
                    builder.settingsNodesWrapper(CommonSetupSteps.wrap(builder.plotManager()));
                    SetupUtils.generators.get(builder.plotManager()).getPlotGenerator()
                            .processAreaSetup(builder);
                } else {
                    builder.plotManager(PlotSquared.imp().getPluginName());
                    MainUtil.sendMessage(plotPlayer, Captions.SETUP_WRONG_GENERATOR);
                    builder.settingsNodesWrapper(CommonSetupSteps.wrap(builder.plotManager()));
                    // TODO why is processSetup not called here?
                }
                if (builder.plotAreaType() == PlotAreaType.PARTIAL) {
                    return CHOOSE_AREA_ID;
                } else {
                    return CHOOSE_TERRAIN_TYPE;
                }
            }
        }

        @Nullable @Override public String getDefaultValue() {
            return PlotAreaType.NORMAL.toString();
        }
    },
    CHOOSE_AREA_ID(Captions.SETUP_AREA_NAME) {

        @Override public SetupStep handleInput(PlotPlayer<?> plotPlayer, PlotAreaBuilder builder, String argument) {
            if (!StringMan.isAlphanumericUnd(argument)) {
                MainUtil.sendMessage(plotPlayer, Captions.SETUP_AREA_NON_ALPHANUMERICAL);
                return this;
            }
            for (PlotArea area : PlotSquared.get().getPlotAreas()) {
                if (area.getId() != null && area.getId().equalsIgnoreCase(argument)) {
                    MainUtil.sendMessage(plotPlayer, Captions.SETUP_AREA_INVALID_ID);
                    return this;
                }
            }
            builder.areaName(argument);
            return CHOOSE_MINIMUM_PLOT_ID;
        }

        @Nullable @Override public String getDefaultValue() {
            return null;
        }
    },
    CHOOSE_MINIMUM_PLOT_ID(Captions.SETUP_AREA_MIN_PLOT_ID) {

        @Override public SetupStep handleInput(PlotPlayer<?> plotPlayer, PlotAreaBuilder builder, String argument) {
            try {
                builder.minimumId(PlotId.fromString(argument));
            } catch (IllegalArgumentException ignored) {
                MainUtil.sendMessage(plotPlayer, Captions.SETUP_AREA_MIN_PLOT_ID_ERROR);
                return this;
            } catch (IllegalStateException ignored) {
                MainUtil.sendMessage(plotPlayer, Captions.SETUP_AREA_PLOT_ID_GREATER_THAN_MINIMUM);
                return this;
            }
            return CHOOSE_MAXIMUM_PLOT_ID;
        }

        @Override public String getDefaultValue() {
            return "0;0";
        }
    },
    CHOOSE_MAXIMUM_PLOT_ID(Captions.SETUP_AREA_MAX_PLOT_ID) {

        @Override public SetupStep handleInput(PlotPlayer<?> plotPlayer, PlotAreaBuilder builder, String argument) {
            try {
                builder.maximumId(PlotId.fromString(argument));
            } catch (IllegalArgumentException ignored) {
                MainUtil.sendMessage(plotPlayer, Captions.SETUP_AREA_MAX_PLOT_ID_ERROR);
                return this;
            } catch (IllegalStateException ignored) {
                MainUtil.sendMessage(plotPlayer, Captions.SETUP_AREA_PLOT_ID_GREATER_THAN_MINIMUM);
                return this;
            }
            return CHOOSE_TERRAIN_TYPE;
        }

        @Override public String getDefaultValue() {
            return "0;0";
        }
    },
    CHOOSE_TERRAIN_TYPE(PlotAreaTerrainType.class, Captions.SETUP_PARTIAL_AREA) {

        @Override public SetupStep handleInput(PlotPlayer<?> plotPlayer, PlotAreaBuilder builder, String argument) {
            Optional<PlotAreaTerrainType> optTerrain;
            if (!(optTerrain = PlotAreaTerrainType.fromString(argument))
                    .isPresent()) {
                MainUtil.sendMessage(plotPlayer, Captions.SETUP_PARTIAL_AREA_ERROR, Captions.SETUP_PARTIAL_AREA);
                return this;
            }
            builder.terrainType(optTerrain.get());
            if (builder.settingsNodesWrapper() == null) {
                builder.settingsNodesWrapper(CommonSetupSteps.wrap(builder.plotManager()));
            }
            SettingsNodesWrapper wrapper = builder.settingsNodesWrapper();
            return wrapper.getFirstStep();
        }

        @Nullable @Override public String getDefaultValue() {
            return PlotAreaTerrainType.NONE.toString();
        }
    },
    CHOOSE_WORLD_NAME(Captions.SETUP_WORLD_NAME) {

        @Override public SetupStep handleInput(PlotPlayer<?> plotPlayer, PlotAreaBuilder builder, String argument) {
            if (!isValidWorldName(argument)) {
                MainUtil.sendMessage(plotPlayer, Captions.SETUP_WORLD_NAME_FORMAT + argument);
                return this;
            }
            if (WorldUtil.IMP.isWorld(argument)) {
                if (PlotSquared.get().hasPlotArea(argument)) {
                    MainUtil.sendMessage(plotPlayer, Captions.SETUP_WORLD_NAME_TAKEN);
                    return this;
                }
                MainUtil.sendMessage(plotPlayer, Captions.SETUP_WORLD_APPLY_PLOTSQUARED);
            }
            builder.worldName(argument);
            plotPlayer.deleteMeta("setup");
            String world;
            if (builder.setupManager() == null) {
                world = SetupUtils.manager.setupWorld(builder);
            } else {
                world = builder.setupManager().setupWorld(builder);
            }
            try {
                plotPlayer.teleport(WorldUtil.IMP.getSpawn(world), TeleportCause.COMMAND);
            } catch (Exception e) {
                plotPlayer.sendMessage("&cAn error occurred. See console for more information");
                e.printStackTrace();
            }
            sendMessage(plotPlayer, Captions.SETUP_FINISHED, builder.worldName());
            return null;
        }

        @Nullable @Override public String getDefaultValue() {
            return null;
        }
    };

    @Getter @NotNull private final Collection<String> suggestions;
    private final Caption description;

    /**
     *
     * @param suggestions the input suggestions for this step
     * @param description the caption describing this step
     */
    CommonSetupSteps(@NotNull Collection<String> suggestions, @NotNull Caption description) {
        this.suggestions = suggestions;
        this.description = description;
    }

    CommonSetupSteps(@NotNull Caption description) {
        this.description = description;
        this.suggestions = Collections.emptyList();
    }

    <E extends Enum<E>> CommonSetupSteps(@NotNull Class<E> argumentType, Caption description) {
        this(enumToStrings(argumentType), description);
    }

    @Override public void announce(PlotPlayer<?> plotPlayer) {
        MainUtil.sendMessage(plotPlayer, this.description);
    }

    private static <E extends Enum<E>> Collection<String> enumToStrings(Class<E> type) {
        return Arrays.stream(type.getEnumConstants()).map(e -> e.toString().toLowerCase()).collect(Collectors.toList());
    }

    private static SettingsNodesWrapper wrap(String plotManager) {
        return new SettingsNodesWrapper(SetupUtils.generators.get(plotManager).getPlotGenerator()
                .getNewPlotArea("CheckingPlotSquaredGenerator", null, null, null)
                .getSettingNodes(), CHOOSE_WORLD_NAME);
    }

    private static boolean isValidWorldName(String s) {
        return s.chars().allMatch((i) -> {
            return i == 95 || i == 45 || i >= 97 && i <= 122 || i >= 65 && i <= 90 || i >= 48 && i <= 57 || i == 46;
        });
    }
}
