package com.intellectualcrafters.plot.commands;

import com.plotsquared.general.commands.Command;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(
        command = "schematic",
        permission = "plots.schematic",
        description = "Schematic command",
        aliases = {"sch"},
        category = CommandCategory.SCHEMATIC,
        usage = "/plot schematic <test|save|saveall|paste>")
public class SchematicCmd extends Command {

    public SchematicCmd() { super(MainCommand.getInstance(), true); }

    private boolean running = false;

    public boolean getRunning() {
        return running;
    }

    public void setRunning(boolean value) {
        this.running = value;
    }
}
