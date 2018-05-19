package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.plotsquared.general.commands.Command;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(
        command = "test",
        permission = "plots.schematic.test",
        category = CommandCategory.SCHEMATIC,
        usage = "/plot schematic test <location>")
public class SchematicTest extends SubCommand {

    public SchematicTest(Command parent, boolean isStatic) { super(parent, isStatic); }

    @Override
    public boolean onCommand(final PlotPlayer player, String[] args) {
        if (args.length != 1) {
            C.COMMAND_SYNTAX.send(player, getUsage());
            return false;
        }

        C.COMMAND_NOT_IMPLEMENTED.send(player, "/plot schematic test");

//        TODO: test
//        if (!Permissions.hasPermission(plr, "plots.schematic.test")) {
//            MainUtil.sendMessage(plr, C.NO_PERMISSION, "plots.schematic.test");
//            return false;
//        }
//        if (args.length < 2) {
//            sendMessage(plr, C.SCHEMATIC_MISSING_ARG);
//            return false;
//        }
//        final Location loc = plr.getLocation();
//        final Plot plot = MainUtil.getPlot(loc);
//        if (plot == null) {
//            sendMessage(plr, C.NOT_IN_PLOT);
//            return false;
//        }
//        file = args[1];
//        schematic = SchematicHandler.manager.getSchematic(file);
//        if (schematic == null) {
//            sendMessage(plr, C.SCHEMATIC_INVALID, "non-existent");
//            return false;
//        }
//        final int l1 = schematic.getSchematicDimension().getX();
//        final int l2 = schematic.getSchematicDimension().getZ();
//        final int length = MainUtil.getPlotWidth(loc.getWorld(), plot.id);
//        if ((l1 < length) || (l2 < length)) {
//            sendMessage(plr, C.SCHEMATIC_INVALID, String.format("Wrong size (x: %s, z: %d) vs %d ", l1, l2, length));
//            break;
//        }
//        sendMessage(plr, C.SCHEMATIC_VALID);
//        break;
//        return true;

        return false;
    }
}