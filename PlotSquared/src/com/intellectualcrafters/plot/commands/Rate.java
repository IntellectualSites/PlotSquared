package com.intellectualcrafters.plot.commands;

import org.bukkit.entity.Player;

import com.intellectualcrafters.plot.C;
import com.intellectualcrafters.plot.PlayerFunctions;
import com.intellectualcrafters.plot.Plot;

/**
 * Created by Citymonstret on 2014-10-02.
 */
public class Rate extends SubCommand {

	/*
	 * String cmd, String permission, String description, String usage, String
	 * alias, CommandCategory category
	 */

	public Rate() {
		super("rate", "plots.rate", "Rate the plot", "rate {0-10}", "rt", CommandCategory.ACTIONS, true);
	}

	@Override
	public boolean execute(Player plr, String... args) {
		if (args.length < 1) {
			sendMessage(plr, C.RATING_NOT_VALID);
			return true;
		}
		if (!PlayerFunctions.isInPlot(plr)) {
			sendMessage(plr, C.NOT_IN_PLOT);
			return true;
		}
		Plot plot = PlayerFunctions.getCurrentPlot(plr);
		if (!plot.hasOwner()) {
			sendMessage(plr, C.RATING_NOT_OWNED);
			return true;
		}
		if (plot.getOwner().equals(plr.getUniqueId())) {
			sendMessage(plr, C.RATING_NOT_YOUR_OWN);
			return true;
		}
		String arg = args[0];
		boolean o = false;
		for (char c : arg.toCharArray()) {
			if (!Character.isDigit(c)) {
				o = true;
				break;
			}
		}
		int rating = 0;
		if (!o) {
			rating = Integer.parseInt(arg);
		}
		if (o || ((rating < 0) || (rating > 10))) {
			sendMessage(plr, C.RATING_NOT_VALID);
			return true;
		}
		// TODO implement check for already rated
		boolean rated = false;
		if (rated) {
			sendMessage(plr, C.RATING_ALREADY_EXISTS, plot.getId().toString());
		}
		// TODO actually do something...
		boolean success = false;
		if (success) {
			sendMessage(plr, C.RATING_APPLIED, plot.getId().toString());
		}
		else {
			sendMessage(plr, C.COMMAND_WENT_WRONG);
		}
		return true;
	}
}
