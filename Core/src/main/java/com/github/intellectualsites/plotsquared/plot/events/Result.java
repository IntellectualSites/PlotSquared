package com.github.intellectualsites.plotsquared.plot.events;

import java.util.HashMap;
import java.util.Map;

/**
 * Enum for {@link CancellablePlotEvent}.
 * <p>
 * DENY: do not allow the event to happen
 * ALLOW: allow the event to continue as normal, subject to standard checks
 * FORCE: force the event to occur, even if normal checks would deny.
 * WARNING: this may have unintended consequences! Make sure you study the appropriate code before using!
 */
public enum Result {

    DENY(0), ACCEPT(1), FORCE(2);

    private static Map<Integer, Result> map = new HashMap<>();

    static {
        for (Result eventResult : Result.values()) {
            map.put(eventResult.value, eventResult);
        }
    }

    private String reason = "Cancelled by external plugin.";
    private int value;

    Result(int value) {
        this.value = value;
    }

    /**
     * Obtain the Result enum associated with the int value
     *
     * @param eventResult the int value
     * @return the corresponding Result
     */
    public static Result valueOf(int eventResult) {
        return map.get(eventResult);
    }

    /**
     * Get int value of enum
     *
     * @return integer value
     */
    public int getValue() {
        return value;
    }

    /**
     * Obtain the reason for the result value (usually only used for DENY)
     *
     * @return Result's reason
     */
    public String getReason() {
        return reason;
    }

    /**
     * Set the reason for the result value (usually only used for DENY)
     */
    public void setReason(String reason) {
        this.reason = reason;
    }
}
