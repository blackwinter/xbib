package org.ghost4j;

import java.awt.Image;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.List;

/**
 * Display callback that stores device output as java Image (on image = one
 * page).
 */
public class ImageWriterDisplayCallback implements DisplayCallback {

    /**
     * Holds document images.
     */
    private List<Image> images;

    /**
     * Constructor.
     */
    public ImageWriterDisplayCallback() {
        images = new ArrayList<>();
    }

    public void displayOpen() throws GhostscriptException {

    }

    public void displayPreClose() throws GhostscriptException {

    }

    public void displayClose() throws GhostscriptException {

    }

    public void displayPreSize(int width, int height, int raster, int format)
            throws GhostscriptException {

    }

    public void displaySize(int width, int height, int raster, int format)
            throws GhostscriptException {

    }

    public void displaySync() throws GhostscriptException {

    }

    public void displayPage(int width, int height, int raster, int format,
                            int copies, int flush, byte[] imageData)
            throws GhostscriptException {
        PageRaster pageRaster = new PageRaster();
        pageRaster.setWidth(width);
        pageRaster.setHeight(height);
        pageRaster.setRaster(raster);
        pageRaster.setFormat(format);
        pageRaster.setData(imageData);
        images.add(converterPageRasterToImage(pageRaster));
    }

    public void displayUpdate(int x, int y, int width, int height) throws GhostscriptException {
    }

    public List<Image> getImages() {
        return images;
    }

    /**
     * Converts a PageRaster object to an Image object. Raster data is supposed
     * to hold RGB image data
     *
     * @param raster Page raster to convert
     * @return An image
     */
    private static Image converterPageRasterToImage(PageRaster raster) {
        DataBufferByte dbb = new DataBufferByte(raster.getData(), raster.getData().length);
        WritableRaster wr = Raster.createInterleavedRaster(dbb,
                raster.getWidth(), raster.getHeight(), raster.getRaster(), 3,
                new int[]{0, 1, 2}, null);
        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        ColorModel cm = new ComponentColorModel(cs, false, false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
        return new BufferedImage(cm, wr, false, null);
    }
}
