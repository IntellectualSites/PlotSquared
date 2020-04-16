/*
 *       _____  _       _    _____                                _
 *      |  __ \| |     | |  / ____|                              | |
 *      | |__) | | ___ | |_| (___   __ _ _   _  __ _ _ __ ___  __| |
 *      |  ___/| |/ _ \| __|\___ \ / _` | | | |/ _` | '__/ _ \/ _` |
 *      | |    | | (_) | |_ ____) | (_| | |_| | (_| | | |  __/ (_| |
 *      |_|    |_|\___/ \__|_____/ \__, |\__,_|\__,_|_|  \___|\__,_|
 *                                    | |
 *                                    |_|
 *            PlotSquared plot management system for Minecraft
 *                  Copyright (C) 2020 IntellectualSites
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/*
This will increase a player's allowed plots by the provided value
/plot debugexec runasync addplots.js <player> <amount>
*/
var uuid = UUIDHandler.getUUID('%s0', null);
if (uuid === null) {
    C_INVALID_PLAYER.send(PlotPlayer, '%s0');
} else if (!MathMan.class.static.isInteger('%s1')) {
    C_NOT_VALID_NUMBER.send(PlotPlayer, '(0, ' + Settings.MAX_PLOTS + ')');
} else {
    var amount = parseInt('%s1');
    var pp = IMP.wrapPlayer(UUIDHandler.getUUIDWrapper().getOfflinePlayer(uuid).player);
    var allowed = pp.getAllowedPlots();
    MainUtil.class.static.sendMessage(PlotPlayer, '$4Setting permission: plots.plot.' + (allowed + amount) + ' for %s0');
    IMP.getEconomyHandler().setPermission("", pp.getName(), 'plots.plot.' + (allowed + amount), true);
}
