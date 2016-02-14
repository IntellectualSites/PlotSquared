package com.plotsquared.sponge.util;

import com.intellectualcrafters.plot.commands.MainCommand;
import com.intellectualcrafters.plot.object.ConsolePlayer;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.plotsquared.sponge.SpongeMain;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
        if (MainCommand.onCommand(pp, cmd.getName(), string.isEmpty() ? new String[]{} : string.split(" "))) {
            return CommandResult.success();
        } else {
            return CommandResult.empty();
        }
    }
    
    @Override
    public List<String> getSuggestions(final CommandSource cmd, final String string) throws CommandException {
        // TODO Auto-generated method stub
        return new ArrayList<>(Collections.singletonList("TEST"));
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
