package org.ghost4j;

/**
 * Interface representing a display callback. A display callback provides method
 * to interact with the Ghostscript interpreter display. This can be useful if
 * you are interested in capturing PDF page rasters. Important: in order
 * to use a display callback, Ghostscript must be initialized with
 * -sDEVICE=display -sDisplayHandle and -dDisplayFormat arguments. Usually set
 * -sDisplayHandle to 0 and use -dDisplayFormat to define how display data will
 * be sent to the displayPage method. -dDisplayFormat=16#804 sets a standard RGB
 * ouput.
 *
 * @see {@link <http://ghostscript.com/doc/9.19/Devices.htm>}
 */
public interface DisplayCallback {

    /**
     * Method called when new device has been opened. This is the first event
     * from this device.
     *
     * @throws org.ghost4j.GhostscriptException
     */
    void displayOpen() throws GhostscriptException;

    /**
     * Method called when device is about to be closed. Device will not be
     * closed until this function returns.
     *
     * @throws org.ghost4j.GhostscriptException
     */
    void displayPreClose() throws GhostscriptException;

    /**
     * Method called when device has been closed. This is the last event from
     * this device.
     *
     * @throws org.ghost4j.GhostscriptException
     */
    void displayClose() throws GhostscriptException;

    /**
     * Method called when device is about to be resized.
     *
     * @param width  Width
     * @param height Height
     * @param raster Raster
     * @param format Format
     * @throws org.ghost4j.GhostscriptException
     */
    void displayPreSize(int width, int height, int raster, int format) throws GhostscriptException;

    /**
     * Method called when device has been resized.
     *
     * @param width  Width
     * @param height Height
     * @param raster Raster
     * @param format Format
     * @throws org.ghost4j.GhostscriptException
     */
    void displaySize(int width, int height, int raster, int format) throws GhostscriptException;

    /**
     * Method called on page flush.
     *
     * @throws org.ghost4j.GhostscriptException
     */
    void displaySync() throws GhostscriptException;

    /**
     * Method called on show page.
     *
     * @param width     Width
     * @param height    Height
     * @param raster    Raster
     * @param format    Format
     * @param copies    Copies
     * @param flush     Flush
     * @param imageData Byte array representing image data. Data layout and order is
     *                  controlled by the -dDisplayFormat argument.
     * @throws org.ghost4j.GhostscriptException
     */
    void displayPage(int width, int height, int raster, int format,
                            int copies, int flush, byte[] imageData) throws GhostscriptException;

    /**
     * Method called to notify whenever a portion of the raster is updated.
     *
     * @param x      X coordinate
     * @param y      Y coordinate
     * @param width  Width
     * @param height Height
     * @throws org.ghost4j.GhostscriptException
     */
    void displayUpdate(int x, int y, int width, int height) throws GhostscriptException;
}
