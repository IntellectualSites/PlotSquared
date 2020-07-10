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
package com.plotsquared.core.components;

import com.google.inject.Inject;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.backup.BackupManager;
import com.plotsquared.core.command.MainCommand;
import com.plotsquared.core.configuration.Captions;
import com.plotsquared.core.configuration.file.YamlConfiguration;
import com.plotsquared.core.configuration.serialization.ConfigurationSerialization;
import com.plotsquared.core.generator.ClassicPlotManagerComponent;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotInventory;
import com.plotsquared.core.plot.PlotItemStack;
import com.plotsquared.core.util.EconHandler;
import com.plotsquared.core.util.InventoryUtil;
import com.plotsquared.core.util.MainUtil;
import com.plotsquared.core.util.PatternUtil;
import com.plotsquared.core.util.Permissions;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.world.item.ItemTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class ComponentPresetManager {

    private final List<ComponentPreset> presets;
    private final String guiName;
    private final EconHandler econHandler;
    private final InventoryUtil inventoryUtil;

    @Inject public ComponentPresetManager(@Nullable final EconHandler econHandler, @NotNull final
        InventoryUtil inventoryUtil) {
        this.econHandler = econHandler;
        this.inventoryUtil = inventoryUtil;
        final File file = new File(Objects.requireNonNull(PlotSquared.platform()).getDirectory(), "components.yml");
        if (!file.exists()) {
            boolean created = false;
            try {
                created = file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (!created) {
                PlotSquared.log(Captions.PREFIX + "Failed to create components.yml");
                this.guiName = "&cInvalid!";
                this.presets = new ArrayList<>();
                return;
            }
        }

        ConfigurationSerialization.registerClass(ComponentPreset.class, "ComponentPreset");

        final YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(file);

        if (!yamlConfiguration.contains("title")) {
            yamlConfiguration.set("title", "&6Plot Components");
            try {
                yamlConfiguration.save(file);
            } catch (IOException e) {
                PlotSquared.log(Captions.PREFIX + "Failed to save default values to components.yml");
                e.printStackTrace();
            }
        }
        this.guiName = yamlConfiguration.getString("title", "&6Plot Components");

        if (yamlConfiguration.contains("presets")) {
            this.presets = yamlConfiguration.getMapList("presets").stream().map(o -> (Map<String, Object>) o)
                .map(ComponentPreset::deserialize).collect(Collectors.toList());
        } else {
            final List<ComponentPreset> defaultPreset =
                Collections.singletonList(new ComponentPreset(ClassicPlotManagerComponent.FLOOR,
                    "##wool", 0, "", "&6D&ai&cs&ec&bo &2F&3l&do&9o&4r",
                    Arrays.asList("&6Spice up your plot floor"), ItemTypes.YELLOW_WOOL));
            yamlConfiguration.set("presets", defaultPreset.stream().map(ComponentPreset::serialize)
                .collect(Collectors.toList()));
            try {
                yamlConfiguration.save(file);
            } catch (final IOException e) {
                PlotSquared.log(Captions.PREFIX + "Failed to save default values to components.yml");
                e.printStackTrace();
            }
            this.presets = defaultPreset;
        }

        MainCommand.getInstance().register(new ComponentCommand(this));
    }

    /**
     * Build the component inventory for a player. This also checks
     * if the player is in a compatible plot, and sends appropriate
     * error messages if not
     *
     * @return Build inventory, if it could be created
     */
    @Nullable public PlotInventory buildInventory(final PlotPlayer player) {
        final Plot plot = player.getCurrentPlot();

        if (plot == null) {
            Captions.NOT_IN_PLOT.send(player);
            return null;
        } else if (!plot.hasOwner()) {
            Captions.PLOT_UNOWNED.send(player);
            return null;
        } else if (!plot.isOwner(player.getUUID()) && !plot.getTrusted().contains(player.getUUID())) {
            Captions.NO_PLOT_PERMS.send(player);
            return null;
        }

        final List<ComponentPreset> allowedPresets = new ArrayList<>(this.presets.size());
        for (final ComponentPreset componentPreset : this.presets) {
            if (!componentPreset.getPermission().isEmpty() && !Permissions
                .hasPermission(player, componentPreset.getPermission())) {
                continue;
            }
            allowedPresets.add(componentPreset);
        }
        final int size = (int) Math.ceil((double) allowedPresets.size() / 9.0D);
        final PlotInventory plotInventory = new PlotInventory(this.inventoryUtil, player, size, this.guiName) {
            @Override public boolean onClick(final int index) {
                if (!player.getCurrentPlot().equals(plot)) {
                    return false;
                }

                if (index < 0 || index >= allowedPresets.size()) {
                    return false;
                }

                final ComponentPreset componentPreset = allowedPresets.get(index);
                if (componentPreset == null) {
                    return false;
                }

                if (plot.getRunning() > 0) {
                    Captions.WAIT_FOR_TIMER.send(player);
                    return false;
                }

                final Pattern pattern = PatternUtil.parse(null, componentPreset.getPattern(), false);
                if (pattern == null) {
                    Captions.PRESET_INVALID.send(player);
                    return false;
                }

                if (componentPreset.getCost() > 0.0D && econHandler != null && plot.getArea().useEconomy()) {
                    if (econHandler.getMoney(player) < componentPreset.getCost()) {
                        Captions.PRESET_CANNOT_AFFORD.send(player);
                        return false;
                    } else {
                        econHandler.withdrawMoney(player, componentPreset.getCost());
                        Captions.REMOVED_BALANCE.send(player, componentPreset.getCost() + "");
                    }
                }

                BackupManager.backup(player, plot, () -> {
                    plot.addRunning();
                    for (Plot current : plot.getConnectedPlots()) {
                        current.setComponent(componentPreset.getComponent().name(), pattern);
                    }
                    MainUtil.sendMessage(player, Captions.GENERATING_COMPONENT);
                    PlotSquared.platform().getGlobalBlockQueue().addEmptyTask(plot::removeRunning);
                });
                return false;
            }
        };


        for (int i = 0; i < allowedPresets.size(); i++) {
            final ComponentPreset preset = allowedPresets.get(i);
            final List<String> lore = new ArrayList<>();
            if (preset.getCost() > 0 && this.econHandler != null && plot.getArea().useEconomy()){
                lore.add(Captions.PRESET_LORE_COST.getTranslated().replace("%cost%",
                    String.format("%.2f", preset.getCost())));
            }
            lore.add(Captions.PRESET_LORE_COMPONENT.getTranslated().replace("%component%",
                preset.getComponent().name().toLowerCase()));
            lore.removeIf(String::isEmpty);
            if (!lore.isEmpty()) {
                lore.add("&6");
            }
            lore.addAll(preset.getDescription());
            plotInventory.setItem(i, new PlotItemStack(preset.getIcon().getId().replace("minecraft:", ""),
                1, preset.getDisplayName(), lore.toArray(new String[0])));
        }

        return plotInventory;
    }

}
