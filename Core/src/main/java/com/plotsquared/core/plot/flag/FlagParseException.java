package com.plotsquared.core.plot.flag;

import com.plotsquared.core.config.Caption;
import com.plotsquared.core.config.CaptionUtility;

public class FlagParseException extends Exception {

    private final PlotFlag<?, ?> flag;
    private final String value;
    private final String errorMessage;

    /**
     * Construct a new flag parse exception to indicate that an attempt to parse a plot
     * flag was unsuccessful.
     *
     * @param flag         Flag instance
     * @param value        Value that failed ot parse
     * @param errorMessage An error message explaining the failure
     * @param args         Arguments used to format the error message
     */
    public FlagParseException(final PlotFlag<?, ?> flag, final String value,
        final Caption errorMessage, final Object... args) {
        super(String.format("Failed to parse flag of type '%s'. Value '%s' was not accepted.",
            flag.getName(), value));
        this.flag = flag;
        this.value = value;
        this.errorMessage = CaptionUtility.format(null, errorMessage, args);
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
    public PlotFlag<?, ?> getFlag() {
        return this.flag;
    }

    /**
     * Get the error message that was supplied by the flag instance.
     *
     * @return Error message.
     */
    public String getErrorMessage() {
        return errorMessage;
    }

}
