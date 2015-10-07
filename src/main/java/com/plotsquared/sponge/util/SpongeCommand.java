package com.plotsquared.sponge.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.util.command.CommandCallable;
import org.spongepowered.api.util.command.CommandException;
import org.spongepowered.api.util.command.CommandResult;
import org.spongepowered.api.util.command.CommandSource;

import java.util.Optional;
import com.intellectualcrafters.plot.commands.MainCommand;
import com.intellectualcrafters.plot.object.ConsolePlayer;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.plotsquared.sponge.SpongeMain;

public class SpongeCommand implements CommandCallable {
    
    @Override
    public CommandResult process(final CommandSource cmd, final String string) throws CommandException {
        final String id = cmd.getIdentifier();
        PlotPlayer pp;
        try {
            final UUID uuid = UUID.fromString(id);
            final Player player = SpongeMain.THIS.getServer().getPlayer(uuid).get();
            pp = SpongeUtil.getPlayer(player);
        } catch (final Exception e) {
            pp = ConsolePlayer.getConsole();
        }
        if (MainCommand.onCommand(pp, cmd.getName(), string.length() == 0 ? new String[] {} : string.split(" "))) {
            return CommandResult.success();
        } else {
            return CommandResult.empty();
        }
    }
    
    @Override
    public List<String> getSuggestions(final CommandSource cmd, final String string) throws CommandException {
        // TODO Auto-generated method stub
        return new ArrayList<>(Arrays.asList("TEST"));
    }
    
    @Override
    public boolean testPermission(final CommandSource cmd) {
        return true;
    }
    
    @Override
    public Optional<? extends Text> getShortDescription(final CommandSource cmd) {
        return Optional.of(Texts.of("Shows plot help"));
    }
    
    @Override
    public Optional<? extends Text> getHelp(final CommandSource cmd) {
        return Optional.of(Texts.of("/plot help"));
    }
    
    @Override
    public Text getUsage(final CommandSource cmd) {
        return Texts.of("/plot <command>");
    }
    
}
