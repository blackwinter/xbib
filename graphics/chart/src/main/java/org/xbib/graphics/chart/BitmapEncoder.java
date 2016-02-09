package org.xbib.graphics.chart;

import org.xbib.graphics.chart.internal.component.Chart;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

/**
 * A helper class with static methods for saving Charts as bitmaps
 */
public final class BitmapEncoder {

    public enum BitmapFormat {
        PNG, JPG, BMP, GIF
    }

    private BitmapEncoder() {
    }

    /**
     * Save a Chart as an image file
     *
     * @param chart
     * @param outputStream
     * @param bitmapFormat
     * @throws IOException
     */
    public static void saveBitmap(Chart chart, OutputStream outputStream, BitmapFormat bitmapFormat) throws IOException {
        BufferedImage bufferedImage = getBufferedImage(chart);
        ImageIO.write(bufferedImage, bitmapFormat.toString().toLowerCase(), outputStream);
        outputStream.close();
    }

    /**
     * Save a chart as a PNG with a custom DPI. The default DPI is 72, which is fine for displaying charts on a
     * computer
     * monitor, but for printing
     * charts, a DPI of around 300 is much better.
     *
     * @param chart
     * @param outputStream
     * @param dpi
     * @throws IOException
     */
    public static void saveBitmapWithDPI(Chart chart, OutputStream outputStream, BitmapFormat bitmapFormat, int dpi) throws IOException {
        double scaleFactor = dpi / 72.0;
        BufferedImage bufferedImage = new BufferedImage((int) (chart.getWidth() * scaleFactor), (int) (chart.getHeight() * scaleFactor), BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = bufferedImage.createGraphics();
        AffineTransform at = graphics2D.getTransform();
        at.scale(scaleFactor, scaleFactor);
        graphics2D.setTransform(at);
        chart.paint(graphics2D, chart.getWidth(), chart.getHeight());
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(bitmapFormat.toString().toLowerCase());
        if (writers.hasNext()) {
            ImageWriter writer = writers.next();
            // instantiate an ImageWriteParam object with default compression options
            ImageWriteParam iwp = writer.getDefaultWriteParam();
            ImageTypeSpecifier typeSpecifier = ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_RGB);
            IIOMetadata metadata = writer.getDefaultImageMetadata(typeSpecifier, iwp);
            if (metadata.isReadOnly() || !metadata.isStandardMetadataFormatSupported()) {
                throw new IllegalArgumentException("It is not possible to set the DPI on a bitmap with " + bitmapFormat + " format!! Try another format.");
            }
            setDPI(metadata, dpi);
            try {
                writer.setOutput(outputStream);
                IIOImage image = new IIOImage(bufferedImage, null, metadata);
                writer.write(null, image, iwp);
            } finally {
                writer.dispose();
            }
        }
    }

    /**
     * Sets the metadata correctly
     *
     * @param metadata
     * @param DPI
     * @throws IIOInvalidTreeException
     */
    private static void setDPI(IIOMetadata metadata, int DPI) throws IIOInvalidTreeException {
        // for PNG, it's dots per millimeter
        double dotsPerMilli = 1.0 * DPI / 10 / 2.54;
        IIOMetadataNode horiz = new IIOMetadataNode("HorizontalPixelSize");
        horiz.setAttribute("value", Double.toString(dotsPerMilli));
        IIOMetadataNode vert = new IIOMetadataNode("VerticalPixelSize");
        vert.setAttribute("value", Double.toString(dotsPerMilli));
        IIOMetadataNode dim = new IIOMetadataNode("Dimension");
        dim.appendChild(horiz);
        dim.appendChild(vert);
        IIOMetadataNode root = new IIOMetadataNode("javax_imageio_1.0");
        root.appendChild(dim);
        metadata.mergeTree("javax_imageio_1.0", root);
    }

    /**
     * Save a Chart as a JPEG file
     *
     * @param chart
     * @param outputStream
     * @param quality  - a float between 0 and 1 (1 = maximum quality)
     * @throws IOException
     */
    public static void saveJPGWithQuality(Chart chart, OutputStream outputStream, float quality) throws IOException {
        BufferedImage bufferedImage = getBufferedImage(chart);
        Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName("jpeg");
        ImageWriter writer = iter.next();
        ImageWriteParam iwp = writer.getDefaultWriteParam();
        iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        iwp.setCompressionQuality(quality);
        try {
            writer.setOutput(outputStream);
            IIOImage image = new IIOImage(bufferedImage, null, null);
            writer.write(null, image, iwp);
            writer.dispose();
        } finally {
            outputStream.close();
        }
    }

    /**
     * Generates a byte[] for a given chart, PNG compressed
     *
     * @param chart
     * @param bitmapFormat
     * @return a byte[] for a given chart, PNG compressed
     * @throws IOException
     */
    public static byte[] getBitmapBytes(Chart chart, BitmapFormat bitmapFormat) throws IOException {
        BufferedImage bufferedImage = getBufferedImage(chart);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, bitmapFormat.toString().toLowerCase(), baos);
        baos.flush();
        byte[] imageInBytes = baos.toByteArray();
        baos.close();
        return imageInBytes;
    }

    public static BufferedImage getBufferedImage(Chart chart) {
        BufferedImage bufferedImage = new BufferedImage(chart.getWidth(), chart.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = bufferedImage.createGraphics();
        chart.paint(graphics2D);
        return bufferedImage;
    }
}
