package com.github.intellectualsites.plotsquared.plot.object;

import com.github.intellectualsites.plotsquared.plot.util.block.BlockUtil;

import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.commands.DebugExec;
import com.github.intellectualsites.plotsquared.plot.commands.MainCommand;

import javax.script.ScriptException;

public abstract class Expression<T> {
    public static <U> Expression<U> constant(final U value) {
        return new Expression<U>() {
            @Override public U evaluate(U arg) {
                return value;
            }
        };
    }

    public static Expression<Double> linearDouble(final Double value) {
        return new Expression<Double>() {
            @Override public Double evaluate(Double arg) {
                return (arg * value);
            }
        };
    }

    public static Expression<Double> doubleExpression(final String expression) {
        try {
            return constant(Double.parseDouble(expression));
        } catch (NumberFormatException ignore) {
        }
        if (expression.endsWith("*{arg}")) {
            try {
                return linearDouble(
                    Double.parseDouble(expression.substring(0, expression.length() - 6)));
            } catch (NumberFormatException ignore) {
            }
        }
        return new Expression<Double>() {
            @Override public Double evaluate(Double arg) {
                DebugExec exec = (DebugExec) MainCommand.getInstance().getCommand(DebugExec.class);
                try {
                    return (Double) exec.getEngine().eval(expression.replace("{arg}", "" + arg));
                } catch (ScriptException e) {
                    PlotSquared.debug("Invalid Expression: " + expression);
                    e.printStackTrace();
                }
                return 0d;
            }
        };
    }

    public abstract T evaluate(T arg);
}
