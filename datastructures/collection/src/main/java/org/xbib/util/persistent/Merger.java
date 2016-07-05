package org.xbib.util.persistent;

public interface Merger<T> {

    void insert(T newEntry);

    /**
     * Return true if newEntry should replace oldEntry, otherwise false.
     *
     * @param oldEntry old entry
     * @param newEntry new entry
     * @return true if newEntry replaces oldEntry.
     */
    boolean merge(T oldEntry, T newEntry);

    void delete(T oldEntry);

}