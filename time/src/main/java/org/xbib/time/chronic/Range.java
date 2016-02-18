package org.xbib.time.chronic;

public class Range {
    private Long begin;
    private Long end;

    public Range(long begin, long end) {
        this.begin = begin;
        this.end = end;
    }

    public Long getBegin() {
        return begin;
    }

    public Long getEnd() {
        return end;
    }

    public Long getWidth() {
        return getEnd() - getBegin();
    }

    public boolean contains(long value) {
        return begin <= value && end >= value;
    }

    @Override
    public int hashCode() {
        return begin.hashCode() * end.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Range && ((Range) obj).begin.equals(begin) && ((Range) obj).end.equals(end);
    }
}
