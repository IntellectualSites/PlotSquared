package com.intellectualsites.commands.test;

import com.intellectualsites.commands.*;
import com.intellectualsites.commands.callers.CommandCaller;
import com.intellectualsites.commands.callers.SystemCaller;

public class CommandTest {

    public static void main(String[] args) {
        CommandCaller caller = new SystemCaller();
        CommandManager manager = new CommandManager();
        if(!manager.createCommand(new TestCommand())) {
            System.out.println("Failed to create command :(");
        }
        manager.handle(caller, "/test banana cow grass");
    }

    @CommandDeclaration(command = "test", usage = "/test [word]")
    public static class TestCommand extends Command {
        TestCommand() {
            requiredArguments = new Argument[] {
                    Argument.String, Argument.String, Argument.String
            };
            addCommand(new Command("banana", new String[0]) {
                @Override
                public boolean onCommand(CommandCaller caller, String[] arguments) {
                    if (getCommands().isEmpty()) {
                        addCommand(new Command("cow") {
                            @Override
                            public boolean onCommand(CommandCaller caller, String[] arguments) {
                                caller.message("I eat " + arguments[0]);
                                return true;
                            }
                        });
                    }
                    handle(caller, arguments);
                    return true;
                }
            });
        }

        @Override
        public boolean onCommand(CommandCaller caller, String[] arguments) {
            handle(caller, arguments);
            return true;
        }
    }
}
