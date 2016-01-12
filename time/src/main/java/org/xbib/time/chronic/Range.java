package org.xbib.time.chronic;

public class Range {
    private long begin;
    private long end;

    public Range(long begin, long end) {
        this.begin = begin;
        this.end = end;
    }

    public long getBegin() {
        return begin;
    }

    public long getEnd() {
        return end;
    }

    public long getWidth() {
        return getEnd() - getBegin();
    }

    /**
     * Returns true if the start and end are the same (i.e. this is a single value).
     */
    public boolean isSingularity() {
        return getEnd() == getBegin();
    }

    public boolean contains(long value) {
        return begin <= value && end >= value;
    }

    @Override
    public int hashCode() {
        return (int) (begin * end);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Range && ((Range) obj).begin == begin && ((Range) obj).end == end;
    }
}
