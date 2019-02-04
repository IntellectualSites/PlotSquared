// Add your commands to this list
var commands = ["mycommand"];

// Command registration:
for (var i in commands) {
    MainCommand.class.static.onCommand(PlotPlayer, "plot", "debugexec", "addcmd", commands[i] + ".js");
}