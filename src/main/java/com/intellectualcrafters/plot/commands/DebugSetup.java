////////////////////////////////////////////////////////////////////////////////////////////////////
// PlotSquared - A plot manager and world generator for the Bukkit API                             /
// Copyright (c) 2014 IntellectualSites/IntellectualCrafters                                       /
//                                                                                                 /
// This program is free software; you can redistribute it and/or modify                            /
// it under the terms of the GNU General Public License as published by                            /
// the Free Software Foundation; either version 3 of the License, or                               /
// (at your option) any later version.                                                             /
//                                                                                                 /
// This program is distributed in the hope that it will be useful,                                 /
// but WITHOUT ANY WARRANTY; without even the implied warranty of                                  /
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                                   /
// GNU General Public License for more details.                                                    /
//                                                                                                 /
// You should have received a copy of the GNU General Public License                               /
// along with this program; if not, write to the Free Software Foundation,                         /
// Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA                               /
//                                                                                                 /
// You can contact us via: support@intellectualsites.com                                           /
////////////////////////////////////////////////////////////////////////////////////////////////////
package com.intellectualcrafters.plot.commands;

import java.util.Map.Entry;

import org.bukkit.generator.ChunkGenerator;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.ConfigurationNode;
import com.plotsquared.bukkit.generator.HybridGen;
import com.plotsquared.bukkit.object.PlotGenerator;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.SetupObject;
import com.intellectualcrafters.plot.util.MainUtil;
import com.plotsquared.bukkit.util.SetupUtils;

public class DebugSetup extends SubCommand {
    public DebugSetup() {
        super("setup", "plots.admin.command.setup", "Plotworld setup command", "setup", "create", CommandCategory.ACTIONS, false);
    }
    
    public void displayGenerators(PlotPlayer plr) {
        StringBuffer message = new StringBuffer();
        message.append("&6What generator do you want?");
        for (Entry<String, ChunkGenerator> entry : SetupUtils.generators.entrySet()) {
            if (entry.getKey().equals("PlotSquared")) {
                message.append("\n&8 - &2" + entry.getKey() + " (Default Generator)");
            }
            else if (entry.getValue() instanceof HybridGen) {
                message.append("\n&8 - &7" + entry.getKey() + " (Hybrid Generator)");
            }
            else if (entry.getValue() instanceof PlotGenerator) {
                message.append("\n&8 - &7" + entry.getKey() + " (Plot Generator)");
            }
            else {
                message.append("\n&8 - &7" + entry.getKey() + " (Unknown structure)");
            }
        }
        MainUtil.sendMessage(plr, message.toString());
    }

    @Override
    public boolean execute(final PlotPlayer plr, final String... args) {
        // going through setup
        String name;
        if (plr == null) {
            name = "*";
        }
        else {
            name = plr.getName();
        }
        if (!SetupUtils.setupMap.containsKey(name)) {
            final SetupObject object = new SetupObject();
            SetupUtils.setupMap.put(name, object);
            SetupUtils.manager.updateGenerators();
            sendMessage(plr, C.SETUP_INIT);
            displayGenerators(plr);
            return false;
        }
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("cancel")) {
                SetupUtils.setupMap.remove(name);
                MainUtil.sendMessage(plr, "&aCancelled setup");
                return false;
            }
            if (args[0].equalsIgnoreCase("back")) {
                final SetupObject object = SetupUtils.setupMap.get(name);
                if (object.setup_index > 0) {
                    object.setup_index--;
                    final ConfigurationNode node = object.step[object.setup_index];
                    sendMessage(plr, C.SETUP_STEP, object.setup_index + 1 + "", node.getDescription(), node.getType().getType(), node.getDefaultValue() + "");
                    return false;
                } else if (object.current > 0) {
                    object.current--;
                }
            }
        }
        final SetupObject object = SetupUtils.setupMap.get(name);
        final int index = object.current;
        switch (index) {
            case 0: { // choose plot manager // skip if 1 option
            }
            case 1: { // choose type (default, augmented, cluster)
            }
            case 2: { // Choose generator (vanilla, non plot generator) // skip if one option
            }
            case 3: { // world setup // skip if one option
            }
            case 4: { // world name
            }
        }
        return false;
    }
    
}
