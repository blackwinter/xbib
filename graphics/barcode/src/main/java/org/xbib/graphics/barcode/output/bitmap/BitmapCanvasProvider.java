
package org.xbib.graphics.barcode.output.bitmap;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

import org.xbib.graphics.barcode.BarcodeDimension;
import org.xbib.graphics.barcode.TextAlignment;
import org.xbib.graphics.barcode.output.AbstractCanvasProvider;
import org.xbib.graphics.barcode.output.java2d.Java2DCanvasProvider;

/**
 * CanvasProvider implementation for generating bitmaps. This class wraps
 * Java2DCanvasProvider to do the actual rendering.
 */
public class BitmapCanvasProvider extends AbstractCanvasProvider {

    private OutputStream out;
    private String mime;
    private int resolution;
    private int imageType;
    private boolean antiAlias;
    private BufferedImage image;
    private Java2DCanvasProvider delegate;

    /**
     * Creates a new BitmapCanvasProvider. 
     * @param out OutputStream to write to
     * @param mime MIME type of the desired output format (ex. "image/png")
     * @param resolution the desired image resolution (dots per inch)
     * @param imageType the desired image type (Values: BufferedImage.TYPE_*)
     * @param antiAlias true if anti-aliasing should be enabled
     */
    public BitmapCanvasProvider(OutputStream out, String mime, 
                    int resolution, int imageType, boolean antiAlias, int orientation) {
        super(orientation);
        this.out = out;
        this.mime = mime;
        this.resolution = resolution;
        this.imageType = imageType;
        this.antiAlias = antiAlias;
    }

    /**
     * Creates a new BitmapCanvasProvider. 
     * @param resolution the desired image resolution (dots per inch)
     * @param imageType the desired image type (Values: BufferedImage.TYPE_*)
     * @param antiAlias true if anti-aliasing should be enabled
     */
    public BitmapCanvasProvider(int resolution, int imageType, boolean antiAlias, 
                    int orientation) {
        this(null, null, resolution, imageType, antiAlias, orientation);
    }

    /**
     * Call this method to finish any pending operations after the 
     * BarcodeGenerator has finished its work.
     * @throws IOException in case of an I/O problem
     */
    public void finish() throws IOException {
        this.image.flush();
        if (this.out != null) {
            final BitmapEncoder encoder = BitmapEncoderRegistry.getInstance(mime);
            encoder.encode(this.image, out, mime, resolution);
        }
    }
    
    /**
     * Returns the buffered image that is used to paint the barcode on.
     * @return the image.
     */
    public BufferedImage getBufferedImage() {
        return this.image;
    }

    /** {@inheritDoc} */
    public void establishDimensions(BarcodeDimension dim) {
        super.establishDimensions(dim);
        this.image = BitmapBuilder.prepareImage(dim, getOrientation(),
                this.resolution, this.imageType);
        this.delegate = new Java2DCanvasProvider(
            BitmapBuilder.prepareGraphics2D(this.image, dim, getOrientation(),
                    this.antiAlias), getOrientation());
        this.delegate.establishDimensions(dim);
    }

    /** {@inheritDoc} */
    public void deviceFillRect(double x, double y, double w, double h) {
        this.delegate.deviceFillRect(x, y, w, h);
    }

    /** {@inheritDoc} */
    public void deviceText(String text,
            double x1, double x2, double y1,
            String fontName, double fontSize, TextAlignment textAlign) {
        this.delegate.deviceText(text, x1, x2, y1, fontName, fontSize, textAlign);
    }

}
