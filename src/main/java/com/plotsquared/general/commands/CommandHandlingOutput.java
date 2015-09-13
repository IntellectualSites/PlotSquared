package com.plotsquared.general.commands;

import java.lang.reflect.Field;

public class CommandHandlingOutput {
    
    public static int CALLER_OF_WRONG_TYPE = -6;
    public static int NOT_COMMAND = -5;
    public static int NOT_FOUND = -4;
    public static int NOT_PERMITTED = -3;
    public static int ERROR = -2;
    public static int WRONG_USAGE = -1;
    
    public static int SUCCESS = 1;
    
    public static String nameField(final int code) {
        final Field[] fields = CommandHandlingOutput.class.getDeclaredFields();
        for (final Field field : fields) {
            if (field.getGenericType() == Integer.TYPE) {
                try {
                    if ((Integer) field.get(CommandHandlingOutput.class) == code) {
                        return field.getName();
                    }
                } catch (final IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return "null??";
    }
}
