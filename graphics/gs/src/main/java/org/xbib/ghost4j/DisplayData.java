package org.ghost4j;

import com.sun.jna.Pointer;

/**
 * Simple class used to store display callback data.
 */
public class DisplayData {

    private int width;
    private int height;
    private int raster;
    private int format;
    private Pointer pimage;

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getRaster() {
        return raster;
    }

    public void setRaster(int raster) {
        this.raster = raster;
    }

    public int getFormat() {
        return format;
    }

    public void setFormat(int format) {
        this.format = format;
    }

    public Pointer getPimage() {
        return pimage;
    }

    public void setPimage(Pointer pimage) {
        this.pimage = pimage;
    }
}
