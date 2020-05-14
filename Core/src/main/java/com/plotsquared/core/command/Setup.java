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
package com.plotsquared.core.command;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.Captions;
import com.plotsquared.core.configuration.ConfigurationNode;
import com.plotsquared.core.configuration.ConfigurationUtil;
import com.plotsquared.core.events.TeleportCause;
import com.plotsquared.core.generator.GeneratorWrapper;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotAreaTerrainType;
import com.plotsquared.core.plot.PlotAreaType;
import com.plotsquared.core.plot.PlotId;
import com.plotsquared.core.plot.SetupObject;
import com.plotsquared.core.plot.message.PlotMessage;
import com.plotsquared.core.util.MainUtil;
import com.plotsquared.core.util.SetupUtils;
import com.plotsquared.core.util.StringMan;
import com.plotsquared.core.util.WorldUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;

@CommandDeclaration(command = "setup",
    permission = "plots.admin.command.setup",
    description = "Setup wizard for plot worlds",
    usage = "/plot setup",
    aliases = {"create"},
    category = CommandCategory.ADMINISTRATION)
public class Setup extends SubCommand {

    private static boolean d(String s) {
        return s.chars().allMatch((i) -> {
            return i == 95 || i == 45 || i >= 97 && i <= 122 || i >= 48 && i <= 57 || i == 46;
        });
    }

    public void displayGenerators(PlotPlayer player) {
        StringBuilder message = new StringBuilder();
        message.append("&6What generator do you want?");
        for (Entry<String, GeneratorWrapper<?>> entry : SetupUtils.generators.entrySet()) {
            if (entry.getKey().equals(PlotSquared.imp().getPluginName())) {
                message.append("\n&8 - &2").append(entry.getKey()).append(" (Default Generator)");
            } else if (entry.getValue().isFull()) {
                message.append("\n&8 - &7").append(entry.getKey()).append(" (Plot Generator)");
            } else {
                message.append("\n&8 - &7").append(entry.getKey()).append(" (Unknown structure)");
            }
        }
        MainUtil.sendMessage(player, message.toString());
    }

    @Override public boolean onCommand(PlotPlayer player, String[] args) {
        // going through setup
        SetupObject object = player.getMeta("setup");
        if (object == null) {
            object = new SetupObject();
            player.setMeta("setup", object);
            SetupUtils.manager.updateGenerators();
            sendMessage(player, Captions.SETUP_INIT);
            displayGenerators(player);
            return false;
        }
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("cancel")) {
                player.deleteMeta("setup");
                MainUtil.sendMessage(player, Captions.SETUP_CANCELLED);
                return false;
            }
            if (args[0].equalsIgnoreCase("back")) {
                if (object.setup_index > 0) {
                    object.setup_index--;
                    ConfigurationNode node = object.step[object.setup_index];
                    sendMessage(player, Captions.SETUP_STEP, object.setup_index + 1,
                        node.getDescription(), node.getType().getType(),
                        String.valueOf(node.getDefaultValue()));
                    return false;
                } else if (object.current > 0) {
                    object.current--;
                }
            }
        }
        int index = object.current;
        switch (index) {
            case 0:  // choose generator
                if (args.length != 1 || !SetupUtils.generators.containsKey(args[0])) {
                    String prefix = "\n&8 - &7";
                    MainUtil.sendMessage(player,
                        Captions.SETUP_WORLD_GENERATOR_ERROR + prefix + StringMan
                            .join(SetupUtils.generators.keySet(), prefix)
                            .replaceAll(PlotSquared.imp().getPluginName(),
                                "&2" + PlotSquared.imp().getPluginName()));
                    sendMessage(player, Captions.SETUP_INIT);
                    return false;
                }
                object.setupGenerator = args[0];
                object.current++;
                MainUtil.sendMessage(player, Captions.SETUP_WORLD_TYPE);
                break;
            case 1:  // choose world type
                List<String> allTypes = Arrays.asList("normal", "augmented", "partial");
                List<String> allDesc = Arrays
                    .asList("Standard plot generation", "Plot generation with vanilla terrain",
                        "Vanilla with clusters of plots");
                ArrayList<String> types = new ArrayList<>();
                if (SetupUtils.generators.get(object.setupGenerator).isFull()) {
                    types.add("normal");
                }
                types.add("augmented");
                types.add("partial");
                Optional<PlotAreaType> plotAreaType;
                if (args.length != 1 || !(plotAreaType = PlotAreaType.fromString(args[0]))
                    .isPresent()) {
                    MainUtil.sendMessage(player, Captions.SETUP_WORLD_TYPE_ERROR);
                    for (String type : types) {
                        int i = allTypes.indexOf(type);
                        if (type.equals("normal")) {
                            MainUtil
                                .sendMessage(player, "&8 - &2" + type + " &8-&7 " + allDesc.get(i));
                        } else {
                            MainUtil
                                .sendMessage(player, "&8 - &7" + type + " &8-&7 " + allDesc.get(i));
                        }
                    }
                    return false;
                }
                object.type = plotAreaType.orElse(PlotAreaType.NORMAL);
                GeneratorWrapper<?> gen = SetupUtils.generators.get(object.setupGenerator);
                if (object.type == PlotAreaType.NORMAL) {
                    object.current = 6;
                    if (object.step == null) {
                        object.plotManager = object.setupGenerator;
                        object.step =
                            SetupUtils.generators.get(object.plotManager).getPlotGenerator()
                                .getNewPlotArea("CheckingPlotSquaredGenerator", null, null, null)
                                .getSettingNodes();
                        SetupUtils.generators.get(object.plotManager).getPlotGenerator()
                            .processSetup(object);
                    }
                    if (object.step.length == 0) {
                        MainUtil.sendMessage(player, Captions.SETUP_WORLD_NAME);
                        object.setup_index = 0;
                        return true;
                    }
                    ConfigurationNode step = object.step[object.setup_index];
                    sendMessage(player, Captions.SETUP_STEP, object.setup_index + 1,
                        step.getDescription(), step.getType().getType(),
                        String.valueOf(step.getDefaultValue()));
                } else {
                    if (gen.isFull()) {
                        object.plotManager = object.setupGenerator;
                        object.setupGenerator = null;
                        object.step =
                            SetupUtils.generators.get(object.plotManager).getPlotGenerator()
                                .getNewPlotArea("CheckingPlotSquaredGenerator", null, null, null)
                                .getSettingNodes();
                        SetupUtils.generators.get(object.plotManager).getPlotGenerator()
                            .processSetup(object);
                    } else {
                        object.plotManager = PlotSquared.imp().getPluginName();
                        MainUtil.sendMessage(player, Captions.SETUP_WRONG_GENERATOR);
                        object.step =
                            SetupUtils.generators.get(object.plotManager).getPlotGenerator()
                                .getNewPlotArea("CheckingPlotSquaredGenerator", null, null, null)
                                .getSettingNodes();
                    }
                    if (object.type == PlotAreaType.PARTIAL) {
                        MainUtil.sendMessage(player, Captions.SETUP_AREA_NAME);
                        object.current++;
                    } else {
                        MainUtil.sendMessage(player, Captions.SETUP_PARTIAL_AREA);
                        object.current = 5;
                    }
                }
                break;
            case 2:  // area id
                if (!StringMan.isAlphanumericUnd(args[0])) {
                    MainUtil.sendMessage(player, Captions.SETUP_AREA_NON_ALPHANUMERICAL);
                    return false;
                }
                for (PlotArea area : PlotSquared.get().getPlotAreas()) {
                    if (area.getId() != null && area.getId().equalsIgnoreCase(args[0])) {
                        MainUtil.sendMessage(player, Captions.SETUP_AREA_INVALID_ID);
                        return false;
                    }
                }
                object.id = args[0];
                object.current++;
                MainUtil.sendMessage(player, Captions.SETUP_AREA_MIN_PLOT_ID);
                break;
            case 3:  // min
                try {
                    object.min = PlotId.fromString(args[0]);
                } catch (IllegalArgumentException ignored) {
                    MainUtil.sendMessage(player, Captions.SETUP_AREA_MIN_PLOT_ID_ERROR);
                    return false;
                }
                object.current++;
                MainUtil.sendMessage(player, Captions.SETUP_AREA_MAX_PLOT_ID);
                break;
            case 4:
                // max
                PlotId id;
                try {
                    id = PlotId.fromString(args[0]);
                } catch (IllegalArgumentException ignored) {
                    MainUtil.sendMessage(player, Captions.SETUP_AREA_MAX_PLOT_ID_ERROR);
                    return false;
                }
                if (id.x <= object.min.x || id.y <= object.min.y) {
                    MainUtil.sendMessage(player, Captions.SETUP_AREA_PLOT_ID_GREATER_THAN_MINIMUM);
                    return false;
                }
                object.max = id;
                object.current++;
                MainUtil.sendMessage(player, Captions.SETUP_PARTIAL_AREA);
                break;
            case 5: { // Choose terrain
                Optional<PlotAreaTerrainType> optTerrain;
                if (args.length != 1 || !(optTerrain = PlotAreaTerrainType.fromString(args[0]))
                    .isPresent()) {
                    MainUtil.sendMessage(player, Captions.SETUP_PARTIAL_AREA_ERROR,
                        Captions.SETUP_PARTIAL_AREA);
                    return false;
                }
                object.terrain = optTerrain.get();
                object.current++;
                if (object.step == null) {
                    object.step = SetupUtils.generators.get(object.plotManager).getPlotGenerator()
                        .getNewPlotArea("CheckingPlotSquaredGenerator", null, null, null)
                        .getSettingNodes();
                }
                ConfigurationNode step = object.step[object.setup_index];
                sendMessage(player, Captions.SETUP_STEP, object.setup_index + 1,
                    step.getDescription(), step.getType().getType(),
                    String.valueOf(step.getDefaultValue()));
                break;
            }
            case 6:  // world setup
                if (object.setup_index == object.step.length) {
                    MainUtil.sendMessage(player, Captions.SETUP_WORLD_NAME);
                    object.setup_index = 0;
                    object.current++;
                    return true;
                }
                ConfigurationNode step = object.step[object.setup_index];
                if (args.length < 1) {
                    sendMessage(player, Captions.SETUP_STEP, object.setup_index + 1,
                        step.getDescription(), step.getType().getType(),
                        String.valueOf(step.getDefaultValue()));
                    return false;
                }

                boolean valid = false;
                try {
                    valid = step.isValid(args[0]);
                } catch (final ConfigurationUtil.UnsafeBlockException e) {
                    Captions.NOT_ALLOWED_BLOCK.send(player, e.getUnsafeBlock().toString());
                }
                if (valid) {
                    step.setValue(args[0]);
                    Object value = step.getValue();
                    sendMessage(player, Captions.SETUP_VALID_ARG, step.getConstant(), value);
                    object.setup_index++;
                    if (object.setup_index == object.step.length) {
                        onCommand(player, args);
                        return false;
                    }
                    step = object.step[object.setup_index];
                    sendMessage(player, Captions.SETUP_STEP, object.setup_index + 1,
                        step.getDescription(), step.getType().getType(),
                        String.valueOf(step.getDefaultValue()));
                    return false;
                } else {
                    sendMessage(player, Captions.SETUP_INVALID_ARG, args[0], step.getConstant());
                    sendMessage(player, Captions.SETUP_STEP, object.setup_index + 1,
                        step.getDescription(), step.getType().getType(),
                        String.valueOf(step.getDefaultValue()));
                    return false;
                }
            case 7:
                if (args.length != 1) {
                    MainUtil.sendMessage(player, Captions.SETUP_WORLD_NAME_ERROR);
                    return false;
                }
                if (!d(args[0])) {
                    MainUtil.sendMessage(player, Captions.SETUP_WORLD_NAME_FORMAT + args[0]);
                    return false;
                }
                if (WorldUtil.IMP.isWorld(args[0])) {
                    if (PlotSquared.get().hasPlotArea(args[0])) {
                        MainUtil.sendMessage(player, Captions.SETUP_WORLD_NAME_TAKEN);
                        return false;
                    }
                    MainUtil.sendMessage(player, Captions.SETUP_WORLD_APPLY_PLOTSQUARED);
                }
                object.world = args[0];
                player.deleteMeta("setup");
                String world;
                if (object.setupManager == null) {
                    world = SetupUtils.manager.setupWorld(object);
                } else {
                    world = object.setupManager.setupWorld(object);
                }
                try {
                    player.teleport(WorldUtil.IMP.getSpawn(world), TeleportCause.COMMAND);
                } catch (Exception e) {
                    player.sendMessage("&cAn error occurred. See console for more information");
                    e.printStackTrace();
                }
                sendMessage(player, Captions.SETUP_FINISHED, object.world);
        }
        return false;
    }


    private static final class StepPickGenerator extends SetupStep {

        @Getter private String generator;

        public StepPickGenerator() {
            super("generator");
        }

        @Override public Collection<PlotMessage> showDescriptionMessage() {
            SetupUtils.manager.updateGenerators();
            final List<PlotMessage> messages = new ArrayList<>();
            messages.add(new PlotMessage("What generator do you want?").color("$6"));
            for (Entry<String, GeneratorWrapper<?>> entry : SetupUtils.generators.entrySet()) {
                final PlotMessage plotMessage = new PlotMessage(" - ").color("$8");
                if (entry.getKey().equals(PlotSquared.imp().getPluginName())) {
                    plotMessage.text(entry.getKey()).color("$8").tooltip("Select this generator")
                        .color("$2").command("/plot setup generator " + entry.getKey())
                        .text(" (Default Generator)").color("$7");
                } else if (entry.getValue().isFull()) {
                    plotMessage.text(entry.getKey()).color("$8").tooltip("Select this generator")
                        .color("$7").command("/plot setup generator " + entry.getKey())
                        .text(" (Plot Generator)").color("$7");
                } else {
                    plotMessage.text(entry.getKey()).color("$8").tooltip("Select this generator")
                        .color("$7").command("/plot setup generator " + entry.getKey())
                        .text(" (Unknown Structure)").color("$7");
                }
                messages.add(plotMessage);
            }
            return messages;
        }

        @Override public boolean parseInput(String input) {
            this.generator = input.toLowerCase();
            return true;
        }

        @Nullable @Override public String getDefault() {
            return null;
        }
    }


    private static final class StepWorldType extends SetupStep {

        private static final Map<String, String> WORLD_TYPES = new HashMap<>();

        static {
            WORLD_TYPES.put("default", "Standard plot generation");
            WORLD_TYPES.put("augmented", "Plot generation with vanilla terrain");
            WORLD_TYPES.put("partial", "Vanilla clusters of plots");
        }

        @Getter private String worldType;

        public StepWorldType() {
            super("type");
        }

        @Override public Collection<PlotMessage> showDescriptionMessage() {
            final List<PlotMessage> messages = new ArrayList<>();
            messages.add(new PlotMessage("What world type do you want?").color("$6"));
            for (final Map.Entry<String, String> worldType : WORLD_TYPES.entrySet()) {
                messages.add(new PlotMessage(" - ").color("$8").text(worldType.getKey())
                    .color(worldType.getKey().equals(getDefault()) ? "$2" : "$7")
                    .tooltip("Select this world type")
                    .command("/plot setup type " + worldType.getKey())
                    .text(" (" + worldType.getValue() + ")").color("$7"));
            }
            return messages;
        }

        @Override public boolean parseInput(String input) {
            if (!WORLD_TYPES.containsKey(input.toLowerCase())) {
                return false;
            }
            this.worldType = input.toLowerCase();
            return true;
        }

        @Override public String getDefault() {
            return "default";
        }
    }


    @ToString
    @EqualsAndHashCode(of = "uuid")
    @AllArgsConstructor
    private static class SetupContext {

        private final UUID uuid;

        @Getter private String step;

    }


    @RequiredArgsConstructor(access = AccessLevel.PROTECTED)
    private abstract static class SetupStep {

        private final String stepName;

        public abstract Collection<PlotMessage> showDescriptionMessage();

        public abstract boolean parseInput(String input);

        public final PlotMessage getUsage() {
            return new PlotMessage("Usage: ").color("$1")
                .text("/plot setup " + this.stepName + " <value>").color("$2").suggest(
                    "/plot setup " + this.stepName + (this.getDefault() != null ?
                        this.getDefault() :
                        ""));
        }

        @Nullable public abstract String getDefault();

        public void sendToPlayer(@NonNull final PlotPlayer plotPlayer) {
            new PlotMessage("Setup Step: ").color("$6").text(this.stepName).color("$7")
                .send(plotPlayer);
            this.getUsage().send(plotPlayer);
            this.showDescriptionMessage().forEach(plotMessage -> plotMessage.send(plotPlayer));
            if (this.getDefault() != null) {
                new PlotMessage("Default: ").color("$6").text(this.getDefault()).color("$7");
            }
        }

    }

}
