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
package com.plotsquared.core.setup;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.caption.Caption;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.events.TeleportCause;
import com.plotsquared.core.generator.GeneratorWrapper;
import com.plotsquared.core.player.MetaDataAccess;
import com.plotsquared.core.player.PlayerMetaDataKeys;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotAreaTerrainType;
import com.plotsquared.core.plot.PlotAreaType;
import com.plotsquared.core.plot.PlotId;
import com.plotsquared.core.util.SetupUtils;
import com.plotsquared.core.util.StringMan;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

public enum CommonSetupSteps implements SetupStep {
    CHOOSE_GENERATOR(TranslatableCaption.of("setup.setup_init")) {
        @Override
        public SetupStep handleInput(PlotPlayer<?> plotPlayer, PlotAreaBuilder builder, String arg) {
            if (!SetupUtils.generators.containsKey(arg)) {
                plotPlayer.sendMessage(TranslatableCaption.of("setup.setup_world_generator_error"));
                return this; // invalid input -> same setup step
            }
            builder.generatorName(arg);
            return CommonSetupSteps.CHOOSE_PLOT_AREA_TYPE; // proceed with next step
        }


        @Override
        public Collection<String> getSuggestions() {
            return Collections.unmodifiableSet(SetupUtils.generators.keySet());
        }

        @Nullable
        @Override
        public String getDefaultValue() {
            return PlotSquared.platform().pluginName();
        }
    },
    CHOOSE_PLOT_AREA_TYPE(PlotAreaType.class, TranslatableCaption.of("setup.setup_world_type")) {
        @Override
        public SetupStep handleInput(PlotPlayer<?> plotPlayer, PlotAreaBuilder builder, String arg) {
            Optional<PlotAreaType> plotAreaType = PlotAreaType.fromString(arg);
            if (!plotAreaType.isPresent()) {
                plotPlayer.sendMessage(TranslatableCaption.of("setup.setup_world_type_error"));
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
                    builder.plotManager(PlotSquared.platform().pluginName());
                    plotPlayer.sendMessage(TranslatableCaption.of("setup.setup_world_generator_error"));
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

        @Nullable
        @Override
        public String getDefaultValue() {
            return PlotAreaType.NORMAL.toString();
        }
    },
    CHOOSE_AREA_ID(TranslatableCaption.of("setup.setup_area_name")) {
        @Override
        public SetupStep handleInput(PlotPlayer<?> plotPlayer, PlotAreaBuilder builder, String argument) {
            if (!StringMan.isAlphanumericUnd(argument)) {
                plotPlayer.sendMessage(TranslatableCaption.of("setup.setup_area_non_alphanumerical"));
                return this;
            }
            for (PlotArea area : PlotSquared.get().getPlotAreaManager().getAllPlotAreas()) {
                if (area.getId() != null && area.getId().equalsIgnoreCase(argument)) {
                    plotPlayer.sendMessage(TranslatableCaption.of("setup.setup_area_invalid_id"));
                    return this;
                }
            }
            builder.areaName(argument);
            return CHOOSE_MINIMUM_PLOT_ID;
        }

        @Nullable
        @Override
        public String getDefaultValue() {
            return null;
        }
    },
    CHOOSE_MINIMUM_PLOT_ID(TranslatableCaption.of("setup.setup_area_min_plot_id")) {
        @Override
        public SetupStep handleInput(PlotPlayer<?> plotPlayer, PlotAreaBuilder builder, String argument) {
            try {
                builder.minimumId(PlotId.fromString(argument));
            } catch (IllegalArgumentException ignored) {
                plotPlayer.sendMessage(TranslatableCaption.of("setup.setup_area_min_plot_id_error"));
                return this;
            } catch (IllegalStateException ignored) {
                plotPlayer.sendMessage(TranslatableCaption.of("setup.setup_area_plot_id_greater_than_minimum"));
                return this;
            }
            return CHOOSE_MAXIMUM_PLOT_ID;
        }

        @Override
        public String getDefaultValue() {
            return "0;0";
        }
    },
    CHOOSE_MAXIMUM_PLOT_ID(TranslatableCaption.of("setup.setup_area_max_plot_id")) {
        @Override
        public SetupStep handleInput(PlotPlayer<?> plotPlayer, PlotAreaBuilder builder, String argument) {
            try {
                builder.maximumId(PlotId.fromString(argument));
            } catch (IllegalArgumentException ignored) {
                plotPlayer.sendMessage(TranslatableCaption.of("setup.setup_area_max_plot_id_error"));
                return this;
            } catch (IllegalStateException ignored) {
                plotPlayer.sendMessage(TranslatableCaption.of("setup.setup_area_plot_id_greater_than_minimum"));
                return this;
            }
            return CHOOSE_TERRAIN_TYPE;
        }

        @Override
        public String getDefaultValue() {
            return "0;0";
        }
    },
    CHOOSE_TERRAIN_TYPE(PlotAreaTerrainType.class, TranslatableCaption.of("setup.setup_partial_area")) {
        @Override
        public SetupStep handleInput(PlotPlayer<?> plotPlayer, PlotAreaBuilder builder, String argument) {
            Optional<PlotAreaTerrainType> optTerrain;
            if (!(optTerrain = PlotAreaTerrainType.fromString(argument))
                    .isPresent()) {
                plotPlayer.sendMessage(TranslatableCaption.of("setup.setup_partial_area_error"));
                return this;
            }
            builder.terrainType(optTerrain.get());
            if (builder.settingsNodesWrapper() == null) {
                builder.settingsNodesWrapper(CommonSetupSteps.wrap(builder.plotManager()));
            }
            SettingsNodesWrapper wrapper = builder.settingsNodesWrapper();
            return wrapper.getFirstStep();
        }

        @Nullable
        @Override
        public String getDefaultValue() {
            return PlotAreaTerrainType.NONE.toString();
        }
    },
    CHOOSE_WORLD_NAME(TranslatableCaption.of("setup.setup_world_name")) {
        @Override
        public SetupStep handleInput(PlotPlayer<?> plotPlayer, PlotAreaBuilder builder, String argument) {
            if (!isValidWorldName(argument)) {
                plotPlayer.sendMessage(TranslatableCaption.of("setup.setup_world_name_format"));
                return this;
            }
            if (PlotSquared.platform().worldUtil().isWorld(argument)) {
                if (PlotSquared.get().getPlotAreaManager().hasPlotArea(argument)) {
                    plotPlayer.sendMessage(
                            TranslatableCaption.of("setup.setup_world_taken"),
                            TagResolver.resolver("value", Tag.inserting(Component.text(argument)))
                    );
                    return this;
                }
                plotPlayer.sendMessage(TranslatableCaption.of("setup.setup_world_apply_plotsquared"));
            }
            builder.worldName(argument);
            try (final MetaDataAccess<SetupProcess> setupAccess = plotPlayer.accessTemporaryMetaData(
                    PlayerMetaDataKeys.TEMPORARY_SETUP)) {
                setupAccess.remove();
            }
            String world;
            if (builder.setupManager() == null) {
                world = PlotSquared.platform().injector().getInstance(SetupUtils.class).setupWorld(builder);
            } else {
                world = builder.setupManager().setupWorld(builder);
            }
            try {
                plotPlayer.teleport(PlotSquared.platform().worldUtil().getSpawn(world), TeleportCause.COMMAND_SETUP);
            } catch (Exception e) {
                plotPlayer.sendMessage(TranslatableCaption.of("errors.error_console"));
                e.printStackTrace();
            }
            plotPlayer.sendMessage(TranslatableCaption.of("setup.setup_finished"));
            return null;
        }

        @Nullable
        @Override
        public String getDefaultValue() {
            return null;
        }
    };

    @NonNull
    private final Collection<String> suggestions;
    private final Caption description;

    /**
     * @param suggestions the input suggestions for this step
     * @param description the caption describing this step
     */
    CommonSetupSteps(@NonNull Collection<String> suggestions, @NonNull Caption description) {
        this.suggestions = suggestions;
        this.description = description;
    }

    CommonSetupSteps(@NonNull Caption description) {
        this.description = description;
        this.suggestions = Collections.emptyList();
    }

    <E extends Enum<E>> CommonSetupSteps(@NonNull Class<E> argumentType, Caption description) {
        this(enumToStrings(argumentType), description);
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
        return s
                .chars()
                .allMatch((i) -> i == 95 || i == 45 || i >= 97 && i <= 122 || i >= 65 && i <= 90 || i >= 48 && i <= 57 || i == 46);
    }

    @Override
    public void announce(PlotPlayer<?> plotPlayer) {
        plotPlayer.sendMessage(this.description);
    }

    public @NonNull Collection<String> getSuggestions() {
        return this.suggestions;
    }
}
