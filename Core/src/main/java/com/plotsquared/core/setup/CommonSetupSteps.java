package com.plotsquared.core.setup;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.Caption;
import com.plotsquared.core.configuration.Captions;
import com.plotsquared.core.configuration.ConfigurationNode;
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

        @Override public SetupStep handleInput(PlotPlayer plotPlayer, PlotAreaBuilder builder, String arg) {
            if (!SetupUtils.generators.containsKey(arg)) {
                String prefix = "\n&8 - &7";
                sendMessage(plotPlayer, Captions.SETUP_WORLD_GENERATOR_ERROR + prefix + StringMan
                        .join(SetupUtils.generators.keySet(), prefix)
                        .replaceAll(PlotSquared.imp().getPluginName(),
                                "&2" + PlotSquared.imp().getPluginName()));
                sendMessage(plotPlayer, Captions.SETUP_INIT);
                return this; // invalid input -> same setup step
            }
            builder.generatorName(arg);
            sendMessage(plotPlayer, Captions.SETUP_WORLD_TYPE);
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

        @Override public SetupStep handleInput(PlotPlayer plotPlayer, PlotAreaBuilder builder, String arg) {
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
            // object.type = plotAreaType.get();
            GeneratorWrapper<?> gen = SetupUtils.generators.get(builder.generatorName());
            if (builder.plotAreaType() == PlotAreaType.NORMAL) {
                if (builder.settingsNodesWrapper() == null) {
                    builder.plotManager(builder.generatorName());
                    builder.settingsNodesWrapper(CommonSetupSteps.wrap(builder.plotManager()));
                    // TODO reimplement SetupUtils.generators.get(object.plotManager).getPlotGenerator()
                    //                            .processSetup(process);
                }
                if (!builder.settingsNodesWrapper().hasNext()) {
                    // MainUtil.sendMessage(plotPlayer, Captions.SETUP_WORLD_NAME);
                    // object.setup_index = 0; TODO what did that do?
                    return CHOOSE_WORLD_NAME; // skip
                }
                SettingsNodeStep next = builder.settingsNodesWrapper().next();
                ConfigurationNode step = next.getConfigurationNode();
                sendMessage(plotPlayer, Captions.SETUP_STEP, next.getId() + 1,
                        step.getDescription(), step.getType().getType(),
                        String.valueOf(step.getDefaultValue()));
                return next;
            } else {
                if (gen.isFull()) {
                    builder.plotManager(builder.generatorName());
                    builder.generatorName(null);
                    builder.settingsNodesWrapper(CommonSetupSteps.wrap(builder.plotManager()));
                    // TODO reimplement SetupUtils.generators.get(object.plotManager).getPlotGenerator()
                    //                            .processSetup(process);
                } else {
                    builder.plotManager(PlotSquared.imp().getPluginName());
                    MainUtil.sendMessage(plotPlayer, Captions.SETUP_WRONG_GENERATOR);
                    builder.settingsNodesWrapper(CommonSetupSteps.wrap(builder.plotManager()));
                    // TODO why is processSetup not called here?
                }
                if (builder.plotAreaType() == PlotAreaType.PARTIAL) {
                    // MainUtil.sendMessage(plotPlayer, Captions.SETUP_AREA_NAME);
                    // TODO return step area id
                    return null;
                } else {
                    // MainUtil.sendMessage(plotPlayer, Captions.SETUP_PARTIAL_AREA);
                    return CHOOSE_TERRAIN_TYPE;
                }
            }
        }

        @Nullable @Override public String getDefaultValue() {
            return PlotAreaType.NORMAL.toString(); // TODO toLowerCase here?
        }
    },
    CHOOSE_AREA_ID(Captions.SETUP_AREA_NAME) {

        @Override public SetupStep handleInput(PlotPlayer plotPlayer, PlotAreaBuilder builder, String argument) {
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

        @Override public SetupStep handleInput(PlotPlayer plotPlayer, PlotAreaBuilder builder, String argument) {
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

        @Override public SetupStep handleInput(PlotPlayer plotPlayer, PlotAreaBuilder builder, String argument) {
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

        @Override public SetupStep handleInput(PlotPlayer plotPlayer, PlotAreaBuilder builder, String argument) {
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
            // TODO return CHOOSE_WORLD_NAME if !hasNext
            return wrapper.hasNext() ? wrapper.next() : wrapper.getAfterwards();
        }

        @Nullable @Override public String getDefaultValue() {
            return PlotAreaTerrainType.NONE.toString();  // TODO toLowerCase here?
        }
    },
    CHOOSE_WORLD_NAME(Captions.SETUP_WORLD_NAME) {

        @Override public SetupStep handleInput(PlotPlayer plotPlayer, PlotAreaBuilder builder, String argument) {
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

    @Override
    public void announce(PlotPlayer plotPlayer) {
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
            return i == 95 || i == 45 || i >= 97 && i <= 122 || i >= 48 && i <= 57 || i == 46;
        });
    }
}
