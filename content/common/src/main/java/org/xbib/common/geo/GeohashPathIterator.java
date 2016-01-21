package org.xbib.common.geo;

import java.util.Iterator;

/**
 * This class iterates over the cells of a given geohash. Assume geohashes
 * form a tree, this iterator traverses this tree form a leaf (actual gehash)
 * to the root (geohash of length 1).
 */
public final class GeohashPathIterator implements Iterator<String> {

    private final String geohash;
    private int currentLength;

    /**
     * Create a new {@link GeohashPathIterator} for a given geohash
     *
     * @param geohash The geohash to traverse
     */
    public GeohashPathIterator(String geohash) {
        this.geohash = geohash;
        this.currentLength = geohash.length();
    }

    @Override
    public boolean hasNext() {
        return currentLength > 0;
    }

    @Override
    public String next() {
        String result = geohash.substring(0, currentLength);
        currentLength--;
        return result;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("unable to remove a geohash from this path");
    }
}