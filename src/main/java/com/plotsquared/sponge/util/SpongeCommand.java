package com.plotsquared.sponge.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.util.command.CommandCallable;
import org.spongepowered.api.util.command.CommandException;
import org.spongepowered.api.util.command.CommandResult;
import org.spongepowered.api.util.command.CommandSource;

import com.google.common.base.Optional;
import com.intellectualcrafters.plot.commands.MainCommand;
import com.intellectualcrafters.plot.object.ConsolePlayer;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.plotsquared.sponge.SpongeMain;

public class SpongeCommand implements CommandCallable {

    @Override
    public CommandResult process(CommandSource cmd, String string) throws CommandException {
        String id = cmd.getIdentifier();
        PlotPlayer pp;
        try {
            UUID uuid = UUID.fromString(id);
            Player player = SpongeMain.THIS.getServer().getPlayer(uuid).get();
            pp = SpongeUtil.getPlayer(player);
        }
        catch (Exception e) {
            pp = ConsolePlayer.getConsole();
        }
        if (MainCommand.onCommand(pp, cmd.getName(), string.length() == 0 ? new String[] {} : string.split(" "))) {
            return CommandResult.success();
        }
        else {
            return CommandResult.empty();
        }
    }

    @Override
    public List<String> getSuggestions(CommandSource cmd, String string) throws CommandException {
        // TODO Auto-generated method stub
        return new ArrayList<>(Arrays.asList("TEST"));
    }

    @Override
    public boolean testPermission(CommandSource cmd) {
        return true;
    }

    @Override
    public Optional<? extends Text> getShortDescription(CommandSource cmd) {
        return Optional.of(Texts.of("Shows plot help"));
    }

    @Override
    public Optional<? extends Text> getHelp(CommandSource cmd) {
        return Optional.of(Texts.of("/plot help"));
    }

    @Override
    public Text getUsage(CommandSource cmd) {
        return Texts.of("/plot <command>");
    }
    
}
