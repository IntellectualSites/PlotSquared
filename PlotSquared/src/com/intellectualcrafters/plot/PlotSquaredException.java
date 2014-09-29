package com.intellectualcrafters.plot;

/**
 * Created by Citymonstret on 2014-09-29.
 */
public class PlotSquaredException extends RuntimeException {

    public PlotSquaredException(PlotError error, String details) {
        super("PlotError >> " + error.getHeader() + ": " + details);
        PlotMain.sendConsoleSenderMessage("&cPlotError &6>> &c" + error.getHeader() + ": &6" + details);
    }

    public static enum PlotError {
        MISSING_DEPENDENCY("Missing Dependency")
        ;
        private String errorHeader;
        PlotError(String errorHeader) {
            this.errorHeader = errorHeader;
        }

        public String getHeader() {
            return this.errorHeader;
        }

        @Override
        public String toString() {
            return this.getHeader();
        }
    }
}
