package com.plotsquared.core.setup;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.Caption;
import com.plotsquared.core.configuration.Captions;
import com.plotsquared.core.configuration.ConfigurationNode;
import com.plotsquared.core.generator.GeneratorWrapper;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.PlotAreaTerrainType;
import com.plotsquared.core.plot.PlotAreaType;
import com.plotsquared.core.util.MainUtil;
import com.plotsquared.core.util.SetupUtils;
import com.plotsquared.core.util.StringMan;
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
    GENERATOR(Captions.SETUP_INIT) {

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
            return CommonSetupSteps.PLOT_AREA_TYPE; // proceed with next step
        }

        @Override public @NotNull Collection<String> getSuggestions() {
            return Collections.unmodifiableSet(SetupUtils.generators.keySet());
        }

        @Override @Nullable public String getDefaultValue() {
            return PlotSquared.imp().getPluginName();
        }
    },
    PLOT_AREA_TYPE(PlotAreaType.class, Captions.SETUP_WORLD_TYPE) {
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
                    return WORLD_NAME; // skip
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
                    return TERRAIN_TYPE;
                }
            }
        }

        @Nullable @Override public String getDefaultValue() {
            return PlotAreaType.NORMAL.toString(); // TODO toLowerCase here?
        }
    },
    TERRAIN_TYPE(PlotAreaTerrainType.class, Captions.SETUP_PARTIAL_AREA) {
        @Override
        public SetupStep handleInput(PlotPlayer plotPlayer, PlotAreaBuilder builder, String argument) {
            return null;
        }

        @Override
        public @Nullable String getDefaultValue() {
            return PlotAreaTerrainType.NONE.toString();  // TODO toLowerCase here?
        }
    },
    WORLD_NAME(Captions.SETUP_WORLD_NAME) {
        @Override public SetupStep handleInput(PlotPlayer plotPlayer, PlotAreaBuilder builder, String argument) {
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
    CommonSetupSteps(@NotNull Collection<String> suggestions, Caption description) {
        this.suggestions = suggestions;
        this.description = description;
    }

    CommonSetupSteps(Caption description) {
        this.description = description;
        this.suggestions = Collections.emptyList();
    }

    <E extends Enum<E>> CommonSetupSteps(Class<E> argumentType, Caption description) {
        this(enumToStrings(argumentType), description);
    }



    private static <E extends Enum<E>> Collection<String> enumToStrings(Class<E> type) {
        return Arrays.stream(type.getEnumConstants()).map(e -> e.toString().toLowerCase()).collect(Collectors.toList());
    }

    private static SettingsNodesWrapper wrap(String plotManager) {
        return new SettingsNodesWrapper(SetupUtils.generators.get(plotManager).getPlotGenerator()
                .getNewPlotArea("CheckingPlotSquaredGenerator", null, null, null)
                .getSettingNodes());
    }

    @Override
    public void announce(PlotPlayer plotPlayer) {
        MainUtil.sendMessage(plotPlayer, this.description);
    }
}
