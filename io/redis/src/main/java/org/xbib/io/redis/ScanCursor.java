package org.xbib.io.redis;

/**
 * Generic Cursor data structure.
 */
public class ScanCursor {

    private String cursor;
    private boolean finished;

    /**
     * Creates a Scan-Cursor reference.
     *
     * @param cursor the cursor id
     * @return ScanCursor
     */
    public static ScanCursor of(String cursor) {
        ScanCursor scanCursor = new ScanCursor();
        scanCursor.setCursor(cursor);
        return scanCursor;
    }

    /**
     * @return cursor id
     */
    public String getCursor() {
        return cursor;
    }

    /**
     * Set the cursor
     *
     * @param cursor the cursor id
     */
    public void setCursor(String cursor) {
        this.cursor = cursor;
    }

    /**
     * @return true if the scan operation of this cursor is finished.
     */
    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }
}
