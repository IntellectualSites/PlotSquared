package com.github.intellectualsites.plotsquared.plot.flags;

public class FlagParseException extends Exception {

    private final PlotFlag<?> flag;
    private final String value;

    public FlagParseException(final PlotFlag<?> flag, final String value) {
        super(String.format("Failed to parse flag of type '%s'. Value '%s' was not accepted.",
            flag.getName(), value));
        this.flag = flag;
        this.value = value;
    }

    /**
     * Returns the value that caused the parse exception
     *
     * @return Value that failed to parse
     */
    public String getValue() {
        return this.value;
    }

    /**
     * Returns the class that threw the exception
     *
     * @return Flag that threw the exception
     */
    public PlotFlag<?> getFlag() {
        return this.flag;
    }

}
