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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.StringMan;
import com.plotsquared.bukkit.BukkitMain;
import com.plotsquared.general.commands.Command;
import com.plotsquared.general.commands.CommandCaller;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(
        command = "toggle",
        aliases = {"attribute"},
        permission = "plots.use",
        description = "Toggle per user settings",
        usage = "/plot toggle <setting>",
        requiredType = RequiredType.NONE,
        category = CommandCategory.ACTIONS
)
public class Toggle extends SubCommand {

    public void noArgs(PlotPlayer plr) {
        MainUtil.sendMessage(plr, C.COMMAND_SYNTAX, "/plot toggle <setting>");
        ArrayList<String> options = new ArrayList<>();
        for (Entry<String, Command<PlotPlayer>> entry : toggles.entrySet()) {
            if (Permissions.hasPermission(plr, entry.getValue().getPermission())) {
                options.add(entry.getKey());
            }
        }
        if (options.size() > 0) {
            MainUtil.sendMessage(plr, C.SUBCOMMAND_SET_OPTIONS_HEADER.s() + StringMan.join(options, ","));
        }
    }
    
    private HashMap<String, Command<PlotPlayer>> toggles;
    
    public Toggle() {
        toggles = new HashMap<>();
        toggles.put("titles", 
                new Command<PlotPlayer>("titles", "/plot toggle titles", "Toggle titles for yourself", C.PERMISSION_PLOT_TOGGLE_TITLES.s()) {
                
                @Override
                public boolean onCommand(PlotPlayer player, String[] args) {
                    if (toggle(player, "disabletitles")) {
                        MainUtil.sendMessage(player, C.TOGGLE_ENABLED, getCommand());
                    }
                    else {
                        MainUtil.sendMessage(player, C.TOGGLE_DISABLED, getCommand());
                    }
                    return true;
                }
        });
        toggles.put("chatspy", 
                new Command<PlotPlayer>("chatspy", "/plot toggle chatspy", "Toggle chat spying", C.PERMISSION_COMMANDS_CHAT.s()) {
                
                @Override
                public boolean onCommand(PlotPlayer player, String[] args) {
                    if (toggle(player, "chatspy")) {
                        MainUtil.sendMessage(player, C.TOGGLE_DISABLED, getCommand());
                    }
                    else {
                        MainUtil.sendMessage(player, C.TOGGLE_ENABLED, getCommand());
                    }
                    return true;
                }
        });
        toggles.put("chat", 
            new Command<PlotPlayer>("chat", "/plot toggle chat", "Toggle plot chat for yourself", C.PERMISSION_PLOT_TOGGLE_CHAT.s()) {
            
            @Override
            public boolean onCommand(PlotPlayer player, String[] args) {
                if (toggle(player, "chat")) {
                    MainUtil.sendMessage(player, C.PLOT_CHAT_OFF);
                }
                else {
                    MainUtil.sendMessage(player, C.PLOT_CHAT_ON);
                }
                return true;
            }
        });
        if (PS.get().worldedit != null) {
            toggles.put("worldedit", 
                new Command<PlotPlayer>("worldedit", "/plot toggle worldedit", "Toggle worldedit bypass", C.PERMISSION_WORLDEDIT_BYPASS.s()) {
                
                @Override
                public boolean onCommand(PlotPlayer player, String[] args) {
                    if (toggle(player, "worldedit")) {
                        MainUtil.sendMessage(player, C.WORLDEDIT_RESTRICTED);
                    }
                    else {
                        MainUtil.sendMessage(player, C.WORLDEDIT_UNMASKED);
                    }
                    return true;
                }
            });
        }
        
    }

    @Override
    public boolean onCommand(final PlotPlayer player, final String[] args) {
        if (args.length == 0) {
            noArgs(player);
            return false;
        }
        Command<PlotPlayer> cmd = toggles.get(args[0].toLowerCase());
        if (cmd == null) {
            noArgs(player);
            return false;
        }
        if (!Permissions.hasPermission(player, cmd.getPermission())) {
            C.NO_PERMISSION.send(player, cmd.getPermission());
            return false;
        }
        return cmd.onCommand(player, Arrays.copyOfRange(args, 1, args.length));
    }
    
    public boolean toggle(PlotPlayer player, String key) {
        if (player.getAttribute(key)) {
            player.removeAttribute(key);
            return true;
        }
        else {
            player.setAttribute(key);
            return false;
        }
    }
}
