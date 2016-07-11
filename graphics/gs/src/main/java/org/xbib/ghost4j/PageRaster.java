package org.ghost4j;

public class PageRaster {

    private int width;
    private int height;
    private int raster;
    private int format;
    private byte[] data;

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

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
