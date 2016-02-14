package com.intellectualcrafters.plot.util.helpmenu;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.util.StringMan;
import com.plotsquared.general.commands.Argument;
import com.plotsquared.general.commands.Command;

public class HelpObject {
    
    private final Command _command;
    private final String _rendered;
    
    public HelpObject(final Command command, final String label) {
        _command = command;
        _rendered = StringMan.replaceAll(C.HELP_ITEM.s(), "%usage%", _command.getUsage().replaceAll("\\{label\\}", label), "[%alias%]",
                !_command.getAliases().isEmpty() ? "(" + StringMan.join(_command.getAliases(), "|") + ")" : "", "%desc%", _command.getDescription(),
                "%arguments%",
        buildArgumentList(_command.getRequiredArguments()), "{label}", label);
    }
    
    @Override
    public String toString() {
        return _rendered;
    }
    
    private String buildArgumentList(final Argument[] arguments) {
        if (arguments == null) {
            return "";
        }
        final StringBuilder builder = new StringBuilder();
        for (final Argument<?> argument : arguments) {
            builder.append("[").append(argument.getName()).append(" (").append(argument.getExample()).append(")],");
        }
        return arguments.length > 0 ? builder.substring(0, builder.length() - 1) : "";
    }
}
