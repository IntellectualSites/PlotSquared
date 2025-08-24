package com.plotsquared.core.exception;

import com.plotsquared.core.configuration.caption.Caption;
import com.plotsquared.core.configuration.caption.LocaleHolder;

/**
 * Internal use only. Used to allow adventure captions to be used in an exception
 *
 * @since TODO
 */
public final class PlotSquaredException extends RuntimeException {

    private final Caption caption;

    /**
     * Create a new instance with the given caption
     *
     * @param caption caption
     */
    public PlotSquaredException(Caption caption) {
        this.caption = caption;
    }

    /**
     * Create a new instance with the given caption and cause
     *
     * @param caption caption
     * @param cause   cause
     */
    public PlotSquaredException(Caption caption, Exception cause) {
        super(cause);
        this.caption = caption;
    }

    @Override
    public String getMessage() {
        return caption.getComponent(LocaleHolder.console());
    }

    public Caption getCaption() {
        return caption;
    }

}
