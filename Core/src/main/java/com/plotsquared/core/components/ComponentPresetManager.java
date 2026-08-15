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
package com.plotsquared.core.components;

import com.google.inject.Inject;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.backup.BackupManager;
import com.plotsquared.core.command.MainCommand;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.configuration.file.YamlConfiguration;
import com.plotsquared.core.configuration.serialization.ConfigurationSerialization;
import com.plotsquared.core.generator.ClassicPlotManagerComponent;
import com.plotsquared.core.permissions.Permission;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotInventory;
import com.plotsquared.core.plot.PlotItemStack;
import com.plotsquared.core.queue.QueueCoordinator;
import com.plotsquared.core.util.EconHandler;
import com.plotsquared.core.util.InventoryUtil;
import com.plotsquared.core.util.PatternUtil;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.world.item.ItemTypes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class ComponentPresetManager {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.builder().build();
    private static final Logger LOGGER = LogManager.getLogger("PlotSquared/" + ComponentPresetManager.class.getSimpleName());

    private final List<ComponentPreset> presets;
    private final EconHandler econHandler;
    private final InventoryUtil inventoryUtil;
    private File componentsFile;

    @SuppressWarnings("unchecked")
    @Inject
    public ComponentPresetManager(final @NonNull EconHandler econHandler, final @NonNull InventoryUtil inventoryUtil) throws
            IOException {
        this.econHandler = econHandler;
        this.inventoryUtil = inventoryUtil;
        final File oldLocation = new File(Objects.requireNonNull(PlotSquared.platform()).getDirectory(), "components.yml");
        final File folder = new File(Objects.requireNonNull(PlotSquared.platform()).getDirectory(), "config");
        if (!folder.exists() && !folder.mkdirs()) {
            LOGGER.error("Failed to create the /plugins/PlotSquared/config folder. Please create it manually");
        }
        if (oldLocation.exists()) {
            Path oldLoc = Paths.get(PlotSquared.platform().getDirectory() + "/components.yml");
            Path newLoc = Paths.get(PlotSquared.platform().getDirectory() + "/config" + "/components.yml");
            Files.move(oldLoc, newLoc);
        }
        try {
            this.componentsFile = new File(folder, "components.yml");
            if (!this.componentsFile.exists() && !this.componentsFile.createNewFile()) {
                LOGGER.error("Could not create the components.yml file. Please create 'components.yml' manually.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        ConfigurationSerialization.registerClass(ComponentPreset.class, "ComponentPreset");

        final YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(this.componentsFile);

        if (yamlConfiguration.contains("title")) {
            yamlConfiguration.set("title", "#Now in /lang/messages_%.json, preset.title");
            try {
                yamlConfiguration.save(this.componentsFile);
            } catch (IOException e) {
                LOGGER.error("Failed to save default values to components.yml", e);
            }
        }

        if (yamlConfiguration.contains("presets")) {
            this.presets = yamlConfiguration
                    .getMapList("presets")
                    .stream()
                    .map(o -> (Map<String, Object>) o)
                    .map(ComponentPreset::deserialize)
                    .collect(Collectors.toList());
        } else {
            final List<ComponentPreset> defaultPreset = Collections.singletonList(
                    new ComponentPreset(
                            ClassicPlotManagerComponent.FLOOR,
                            "##wool",
                            0,
                            "",
                            "<rainbow:2>Disco Floor</rainbow>",
                            List.of("<gold>Spice up your plot floor</gold>"),
                            ItemTypes.YELLOW_WOOL
                    ));
            yamlConfiguration.set("presets", defaultPreset.stream().map(ComponentPreset::serialize).collect(Collectors.toList()));
            try {
                yamlConfiguration.save(this.componentsFile);
            } catch (final IOException e) {
                LOGGER.error("Failed to save default values to components.yml", e);
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
    public @Nullable PlotInventory buildInventory(final PlotPlayer<?> player) {
        final Plot plot = player.getCurrentPlot();

        if (plot == null) {
            player.sendMessage(TranslatableCaption.of("errors.not_in_plot"));
            return null;
        } else if (!plot.hasOwner()) {
            player.sendMessage(TranslatableCaption.of("info.plot_unowned"));
            return null;
        } else if (!plot.isOwner(player.getUUID()) && !plot.getTrusted().contains(player.getUUID()) && !player.hasPermission(
                Permission.PERMISSION_ADMIN_COMPONENTS_OTHER
        )) {
            player.sendMessage(TranslatableCaption.of("permission.no_plot_perms"));
            return null;
        } else if (plot.getVolume() > Integer.MAX_VALUE) {
            player.sendMessage(TranslatableCaption.of("schematics.schematic_too_large"));
            return null;
        }

        final List<ComponentPreset> allowedPresets = new ArrayList<>(this.presets.size());
        for (final ComponentPreset componentPreset : this.presets) {
            if (!componentPreset.permission().isEmpty() && !player.hasPermission(
                    componentPreset.permission()
            )) {
                continue;
            }
            allowedPresets.add(componentPreset);
        }
        if (allowedPresets.isEmpty()) {
            player.sendMessage(TranslatableCaption.of("preset.empty"));
            return null;
        }
        final int size = (int) Math.ceil((double) allowedPresets.size() / 9.0D);
        final PlotInventory plotInventory = new PlotInventory(this.inventoryUtil, player, size,
                TranslatableCaption.of("preset.title").getComponent(player)
        ) {
            @Override
            public boolean onClick(final int index) {
                if (!getPlayer().getCurrentPlot().equals(plot)) {
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
                    getPlayer().sendMessage(TranslatableCaption.of("errors.wait_for_timer"));
                    return false;
                }

                final Pattern pattern = PatternUtil.parse(null, componentPreset.pattern(), false);
                if (pattern == null) {
                    getPlayer().sendMessage(TranslatableCaption.of("preset.preset_invalid"));
                    return false;
                }

                if (componentPreset.cost() > 0.0D && !player.hasPermission(Permission.PERMISSION_ADMIN_BYPASS_ECON)) {
                    if (!econHandler.isEnabled(plot.getArea())) {
                        getPlayer().sendMessage(
                                TranslatableCaption.of("preset.economy_disabled"),
                                TagResolver.resolver("preset", Tag.inserting(Component.text(componentPreset.displayName())))
                        );
                        return false;
                    }
                    if (econHandler.getMoney(getPlayer()) < componentPreset.cost()) {
                        getPlayer().sendMessage(TranslatableCaption.of("preset.preset_cannot_afford"));
                        return false;
                    } else {
                        econHandler.withdrawMoney(getPlayer(), componentPreset.cost());
                        getPlayer().sendMessage(
                                TranslatableCaption.of("economy.removed_balance"),
                                TagResolver.resolver(
                                        "money",
                                        Tag.inserting(Component.text(econHandler.format(componentPreset.cost())))
                                )
                        );
                    }
                }

                BackupManager.backup(getPlayer(), plot, () -> {
                    plot.addRunning();
                    QueueCoordinator queue = plot.getArea().getQueue();
                    queue.setCompleteTask(plot::removeRunning);
                    for (Plot current : plot.getConnectedPlots()) {
                        current.getPlotModificationManager().setComponent(
                                componentPreset.component().name(),
                                pattern,
                                player,
                                queue
                        );
                    }
                    queue.enqueue();
                    getPlayer().sendMessage(TranslatableCaption.of("working.generating_component"));
                });
                return false;
            }
        };


        for (int i = 0; i < allowedPresets.size(); i++) {
            final ComponentPreset preset = allowedPresets.get(i);
            final List<String> lore = new ArrayList<>();
            if (preset.cost() > 0) {
                if (!this.econHandler.isEnabled(plot.getArea())) {
                    lore.add(MINI_MESSAGE.serialize(MINI_MESSAGE.deserialize(
                            TranslatableCaption.of("preset.preset_lore_economy_disabled").getComponent(player))));
                } else {
                    lore.add(MINI_MESSAGE.serialize(MINI_MESSAGE.deserialize(
                            TranslatableCaption.of("preset.preset_lore_cost").getComponent(player),
                            TagResolver.resolver("cost", Tag.inserting(Component.text(String.format("%.2f", preset.cost()))))
                    )));
                }
            }
            lore.add(MINI_MESSAGE.serialize(MINI_MESSAGE.deserialize(
                    TranslatableCaption.of("preset.preset_lore_component").getComponent(player),
                    TagResolver.builder()
                            .tag("component", Tag.inserting(Component.text(preset.component().name().toLowerCase())))
                            .tag("prefix", Tag.inserting(TranslatableCaption.of("core.prefix").toComponent(player)))
                            .build()
            )));
            lore.removeIf(String::isEmpty);
            lore.addAll(preset.description());
            plotInventory.setItem(
                    i,
                    new PlotItemStack(
                            preset.icon().getId().replace("minecraft:", ""),
                            1,
                            preset.displayName(),
                            lore.toArray(new String[0])
                    )
            );
        }

        return plotInventory;
    }

}
