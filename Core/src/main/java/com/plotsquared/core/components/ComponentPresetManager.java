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
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.components;

import com.google.inject.Inject;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.backup.BackupManager;
import com.plotsquared.core.command.MainCommand;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.configuration.file.YamlConfiguration;
import com.plotsquared.core.configuration.serialization.ConfigurationSerialization;
import com.plotsquared.core.generator.ClassicPlotManagerComponent;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotInventory;
import com.plotsquared.core.plot.PlotItemStack;
import com.plotsquared.core.queue.QueueCoordinator;
import com.plotsquared.core.util.EconHandler;
import com.plotsquared.core.util.InventoryUtil;
import com.plotsquared.core.util.PatternUtil;
import com.plotsquared.core.util.Permissions;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.world.item.ItemTypes;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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

    private static final MiniMessage MINI_MESSAGE = MiniMessage.builder().build();
    private static final Logger logger = LoggerFactory.getLogger("P2/" + ComponentPresetManager.class.getSimpleName());

    private final List<ComponentPreset> presets;
    private final String guiName;
    private final EconHandler econHandler;
    private final InventoryUtil inventoryUtil;

    @Inject public ComponentPresetManager(@Nullable final EconHandler econHandler, @Nonnull final InventoryUtil inventoryUtil) {
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
                logger.error("[P2] Failed to create components.yml");
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
                logger.error("[P2] Failed to save default values to components.yml", e);
            }
        }
        this.guiName = yamlConfiguration.getString("title", "&6Plot Components");

        if (yamlConfiguration.contains("presets")) {
            this.presets = yamlConfiguration.getMapList("presets").stream().map(o -> (Map<String, Object>) o).map(ComponentPreset::deserialize)
                .collect(Collectors.toList());
        } else {
            final List<ComponentPreset> defaultPreset = Collections.singletonList(
                new ComponentPreset(ClassicPlotManagerComponent.FLOOR, "##wool", 0, "", "&6D&ai&cs&ec&bo &2F&3l&do&9o&4r",
                    Arrays.asList("<gold>Spice up your plot floor</gold>"), ItemTypes.YELLOW_WOOL));
            yamlConfiguration.set("presets", defaultPreset.stream().map(ComponentPreset::serialize).collect(Collectors.toList()));
            try {
                yamlConfiguration.save(file);
            } catch (final IOException e) {
                logger.error("[P2] Failed to save default values to components.yml", e);
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
     * @param player player
     * @return Build inventory, if it could be created
     */
    @Nullable public PlotInventory buildInventory(final PlotPlayer<?> player) {
        final Plot plot = player.getCurrentPlot();

        if (plot == null) {
            player.sendMessage(TranslatableCaption.of("errors.not_in_plot"));
            return null;
        } else if (!plot.hasOwner()) {
            player.sendMessage(TranslatableCaption.of("info.plot_unowned"));
            return null;
        } else if (!plot.isOwner(player.getUUID()) && !plot.getTrusted().contains(player.getUUID())) {
            player.sendMessage(TranslatableCaption.of("permission.no_plot_perms"));
            return null;
        }

        final List<ComponentPreset> allowedPresets = new ArrayList<>(this.presets.size());
        for (final ComponentPreset componentPreset : this.presets) {
            if (!componentPreset.getPermission().isEmpty() && !Permissions.hasPermission(player, componentPreset.getPermission())) {
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
                    player.sendMessage(TranslatableCaption.of("errors.wait_for_timer"));
                    return false;
                }

                final Pattern pattern = PatternUtil.parse(null, componentPreset.getPattern(), false);
                if (pattern == null) {
                    player.sendMessage(TranslatableCaption.of("preset.preset_invalid"));
                    return false;
                }

                if (componentPreset.getCost() > 0.0D && econHandler != null && plot.getArea().useEconomy()) {
                    if (econHandler.getMoney(player) < componentPreset.getCost()) {
                        player.sendMessage(TranslatableCaption.of("preset.preset_cannot_afford"));
                        return false;
                    } else {
                        econHandler.withdrawMoney(player, componentPreset.getCost());
                        player.sendMessage(TranslatableCaption.of("economy.removed_balance"),
                                Template.of("money", componentPreset.getCost() + ""));
                    }
                }

                BackupManager.backup(player, plot, () -> {
                    plot.addRunning();
                    QueueCoordinator queue = plot.getArea().getQueue();
                    for (Plot current : plot.getConnectedPlots()) {
                        current.setComponent(componentPreset.getComponent().name(), pattern, queue);
                    }
                    queue.setCompleteTask(plot::removeRunning);
                    queue.enqueue();
                    player.sendMessage(TranslatableCaption.of("working.generating_component"));
                });
                return false;
            }
        };


        for (int i = 0; i < allowedPresets.size(); i++) {
            final ComponentPreset preset = allowedPresets.get(i);
            final List<String> lore = new ArrayList<>();
            if (preset.getCost() > 0 && this.econHandler != null && plot.getArea().useEconomy()) {
                lore.add(MINI_MESSAGE.serialize(MINI_MESSAGE.parse(TranslatableCaption.of("preset.preset_lore_cost").getComponent(player),
                    Template.of("cost", String.format("%.2f", preset.getCost())))));
            }
            lore.add(MINI_MESSAGE.serialize(MINI_MESSAGE.parse(TranslatableCaption.of("preset.preset_lore_component").getComponent(player),
                Template.of("component", preset.getComponent().name().toLowerCase()))));
            lore.removeIf(String::isEmpty);
            if (!lore.isEmpty()) {
                lore.add("<gold>");
            }
            lore.addAll(preset.getDescription());
            lore.add("</gold>");
            plotInventory.setItem(i,
                new PlotItemStack(preset.getIcon().getId().replace("minecraft:", ""), 1, preset.getDisplayName(), lore.toArray(new String[0])));
        }

        return plotInventory;
    }

}
