package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.C;
import com.intellectualcrafters.plot.PlayerFunctions;
import com.intellectualcrafters.plot.PlotMain;
import com.intellectualcrafters.plot.SchematicHandler;
import org.bukkit.entity.Player;

public class Schematic extends SubCommand {

    public Schematic() {
        super("schematic", "plots.admin", "Schematic Command", "schematic {arg}", "sch", CommandCategory.ACTIONS);
    }


    @Override
    public boolean execute(Player plr, String... args) {
        if(args.length < 1) {
            sendMessage(plr, C.SCHEMATIC_MISSING_ARG);
            return true;
        }
        String arg = args[0];
        String file;
        SchematicHandler.Schematic schematic;
        switch(arg) {
            case "paste":
                if(args.length < 2) {
                    sendMessage(plr, C.SCHEMATIC_MISSING_ARG);
                    break;
                }
                if(!PlayerFunctions.isInPlot(plr)) {
                    sendMessage(plr, C.NOT_IN_PLOT);
                    break;
                }
                file = args[1];
                schematic = new SchematicHandler().getSchematic(file);
                boolean s = new SchematicHandler().paste(plr.getLocation(), schematic, PlayerFunctions.getCurrentPlot(plr));
                if(s) {
                    sendMessage(plr, C.SCHEMATIC_PASTE_SUCCESS);
                } else {
                    sendMessage(plr, C.SCHEMATIC_PASTE_FAILED);
                }
                break;
            case "test":
                if(args.length < 2) {
                    sendMessage(plr, C.SCHEMATIC_MISSING_ARG);
                    break;
                }
                file = args[1];
                schematic = new SchematicHandler().getSchematic(file);
                if(schematic == null) {
                    sendMessage(plr, C.SCHEMATIC_INVALID, "non-existent");
                    break;
                }
                int l1 = schematic.getSchematicDimension().getX();
                int l2 = schematic.getSchematicDimension().getZ();
                int length = PlotMain.getWorldSettings(plr.getWorld()).PLOT_WIDTH;
                if(l1 != length || l2 != length) {
                    sendMessage(plr, C.SCHEMATIC_INVALID, String.format("Wrong size (x: %s, z: %d) vs %d ", l1, l2, length));
                    break;
                }
                sendMessage(plr, C.SCHEMATIC_VALID);
                break;
            default:
                sendMessage(plr, C.SCHEMATIC_MISSING_ARG);
                break;
        }
        return true;
    }
}
