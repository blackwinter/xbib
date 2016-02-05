package org.xbib.time.chronic.repeaters;

import org.xbib.time.chronic.Span;
import org.xbib.time.chronic.tags.Pointer.PointerType;

public class RepeaterSeason extends RepeaterUnit {
    public static final int SEASON_SECONDS = 7862400; // (91 * 24 * 60 * 60);

    @Override
    protected Span _nextSpan(PointerType pointer) {
        throw new IllegalStateException("Not implemented.");
    }

    @Override
    protected Span _thisSpan(PointerType pointer) {
        throw new IllegalStateException("Not implemented.");
    }

    @Override
    public Span getOffset(Span span, int amount, PointerType pointer) {
        throw new IllegalStateException("Not implemented.");
    }

    @Override
    public int getWidth() {
        // WARN: Does not use Calendar
        return RepeaterSeason.SEASON_SECONDS;
    }

    @Override
    public String toString() {
        return super.toString() + "-season";
    }
}
