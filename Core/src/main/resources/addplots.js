/*
This will increase a player's allowed plots by the provided value
/plot debugexec runasync addperm <player> <amount>
*/
var uuid = UUIDHandler.getUUID('%s0', null);
if (uuid === null) {
    C_INVALID_PLAYER.send(PlotPlayer, '%s0');    
}
else if (!MathMan.class.static.isInteger('%s1')) {
    C_NOT_VALID_NUMBER.send(PlotPlayer, '(0, ' + Settings.MAX_PLOTS + ')');
}
else {
    var amount = parseInt('%s1');
    var pp = IMP.wrapPlayer(UUIDHandler.getUUIDWrapper().getOfflinePlayer(uuid).player);
    var allowed = pp.getAllowedPlots();
    MainUtil.class.static.sendMessage(PlotPlayer, '$4Setting permission: plots.plot.' + (allowed + amount) + ' for %s0');
    IMP.getEconomyHandler().setPermission("", pp.getName(), 'plots.plot.' + (allowed + amount), true);
}
