package com.intellectualcrafters.plot.util.helpmenu;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.util.StringMan;
import com.intellectualsites.commands.Argument;
import com.intellectualsites.commands.Command;

public class HelpObject {

    private final Command _command;
    private final String _rendered;

    public HelpObject(final Command command) {
        this._command = command;
        String rendered = C.HELP_ITEM.s();
        this._rendered = rendered
                .replace("%usage%", _command.getUsage())
                .replace("%alias%", _command.getAliases().size() > 0 ? StringMan.join(_command.getAliases(), "|") : "")
                .replace("%desc%", _command.getDescription())
                .replace("%arguments%", buildArgumentList(_command.getRequiredArguments()));  // TODO Make configurable
    }

    @Override
    public String toString() {
        return _rendered;
    }

    private String buildArgumentList(Argument[] arguments) {
        StringBuilder builder = new StringBuilder();
        for (final Argument argument : arguments) {
            builder.append("[").append(argument.getName()).append(" (").append(argument.getExample()).append(")],");
        }
        return arguments.length > 0 ? builder.substring(0, builder.length() - 1) : "";
    }
}
