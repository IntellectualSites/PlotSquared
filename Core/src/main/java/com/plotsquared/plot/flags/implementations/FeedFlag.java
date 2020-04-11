package com.plotsquared.plot.flags.implementations;

import com.plotsquared.config.Captions;
import com.plotsquared.plot.flags.FlagParseException;
import com.plotsquared.plot.flags.types.TimedFlag;
import org.jetbrains.annotations.NotNull;

public class FeedFlag extends TimedFlag<Integer, FeedFlag> {
    public static final FeedFlag FEED_NOTHING = new FeedFlag(new Timed<>(0, 0));

    public FeedFlag(@NotNull Timed<Integer> value) {
        super(value, 1, Captions.FLAG_DESCRIPTION_FEED);
    }

    @Override protected Integer parseValue(String input) throws FlagParseException {
        int parsed;
        try {
            parsed = Integer.parseInt(input);
        } catch (Throwable throwable) {
            throw new FlagParseException(this, input, Captions.NOT_A_NUMBER, input);
        }
        if (parsed < 1) {
            throw new FlagParseException(this, input, Captions.NUMBER_NOT_POSITIVE, parsed);
        }
        return parsed;
    }

    @Override protected Integer mergeValue(Integer other) {
        return this.getValue().getValue() + other;
    }

    @Override public String getExample() {
        return "10 5";
    }

    @Override protected FeedFlag flagOf(@NotNull Timed<Integer> value) {
        return new FeedFlag(value);
    }
}
