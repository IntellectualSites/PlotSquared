package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.ConsolePlayer;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.SchematicHandler;
import com.plotsquared.general.commands.Command;
import com.plotsquared.general.commands.CommandDeclaration;

import java.util.Collection;

@CommandDeclaration(
        command = "saveall",
        aliases = {"exportall"},
        permission = "plots.admin.command.schematic.save",
        description = "Schematic command",
        category = CommandCategory.SCHEMATIC,
        usage = "/plot schematic saveall <area>")
public class SchematicSaveall extends SubCommand {

    public SchematicSaveall(Command parent, boolean isStatic) { super(parent, isStatic); }

    @Override
    public boolean onCommand(final PlotPlayer player, String[] args) {
        if (!(player instanceof ConsolePlayer)) {
            MainUtil.sendMessage(player, C.NOT_CONSOLE);
            return false;
        }

        if (args.length != 1) {
            C.COMMAND_SYNTAX.send(player, getUsage());
            return false;
        }

        PlotArea area = PS.get().getPlotAreaByString(args[0]);
        if (area == null) {
            C.NOT_VALID_PLOT_WORLD.send(player, args[0]);
            return false;
        }
        Collection<Plot> plots = area.getPlots();
        if (plots.isEmpty()) {
            MainUtil.sendMessage(player, "&cInvalid world. Use &7/plot sch exportall <area>");
            return false;
        }
        boolean result = SchematicHandler.manager.exportAll(plots, null, null, new Runnable() {
            @Override
            public void run() {
                MainUtil.sendMessage(player, "&aFinished mass export");
            }
        });
        if (!result) {
            MainUtil.sendMessage(player, "&cTask is already running.");
            return false;
        } else {
            MainUtil.sendMessage(player, "&3Plot&8->&3Schematic&8: &7Mass export has started. This may take a while.");
            MainUtil.sendMessage(player, "&3Plot&8->&3Schematic&8: &7Found &c" + plots.size() + "&7 plots...");
        }

        return true;
    }
}
