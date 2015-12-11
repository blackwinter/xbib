package org.xbib.graphics.barcode.output.java2d;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.xbib.graphics.barcode.BarcodeDimension;
import org.xbib.graphics.barcode.TextAlignment;
import org.xbib.graphics.barcode.output.AbstractCanvasProvider;

/**
 * CanvasProvider implementation that renders to Java2D (AWT).
 */
public class Java2DCanvasProvider extends AbstractCanvasProvider {

    private Graphics2D g2d;

    /**
     * Creates a new Java2DCanvasProvider.
     * This class internally operates with millimeters (mm) as units. This
     * means you have to apply the necessary transformation before rendering
     * a barcode to obtain the expected size. See the source code for 
     * BitmapBuilder.java for an example.
     * To improve the quality of text output it is recommended that fractional
     * font metrics be enabled on the Graphics2D object passed in:
     * g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
     * RenderingHints.VALUE_FRACTIONALMETRICS_ON);
     * @param g2d Graphics2D object to paint on
     */
    public Java2DCanvasProvider(Graphics2D g2d, int orientation) {
        super(orientation);
        setGraphics2D(g2d);
    }
    
    /**
     * Sets the Graphics2D instance to paint on
     * @param g2d the Graphics2D instance
     */
    public void setGraphics2D(Graphics2D g2d) {
        this.g2d = g2d;
    }

    /**
     * Returns the Graphics2D in use.
     * @return the Graphics2D instance to paint on
     */
    public Graphics2D getGraphics2D() {
        return this.g2d;
    }
    
    /** {@inheritDoc} */
    public void establishDimensions(BarcodeDimension dim) {
        super.establishDimensions(dim);
        int orientation = BarcodeDimension.normalizeOrientation(getOrientation());
        double w = dim.getWidthPlusQuiet(orientation);
        double h = dim.getHeightPlusQuiet(orientation);
        this.g2d = (Graphics2D)this.g2d.create();
        switch (orientation) {
        case 90:
            g2d.rotate(-Math.PI / 2);
            g2d.translate(-h, 0);
            break;
        case 180:
            g2d.rotate(-Math.PI);
            g2d.translate(-w, -h);
            break;
        case 270:
            g2d.rotate(-Math.PI * 1.5);
            g2d.translate(0, -w);
            break;
        default:
            //nop
        }
    }

    /** {@inheritDoc} */
    public void deviceFillRect(double x, double y, double w, double h) {
        g2d.fill(new Rectangle2D.Double(x, y, w, h));
    }

    /** {@inheritDoc} */
    public void deviceDrawRect(double x, double y, double w, double h) {
        g2d.draw(new Rectangle2D.Double(x, y, w, h));
    }

    /** {@inheritDoc} */
    public void deviceText(
            String text,
            double x1,
            double x2,
            double y1,
            String fontName,
            double fontSize,
            TextAlignment textAlign) {
        Font font = new Font(fontName, Font.PLAIN,
            (int)Math.round(fontSize));
        FontRenderContext frc = g2d.getFontRenderContext();
        GlyphVector gv = font.createGlyphVector(frc, text);
        
        final float textwidth = (float)gv.getLogicalBounds().getWidth();
        final float distributableSpace = (float)((x2 - x1) - textwidth);
        final float intercharSpace;
        if (gv.getNumGlyphs() > 1) {
            intercharSpace = distributableSpace / (gv.getNumGlyphs() - 1);
        } else {
            intercharSpace = 0.0f;
        }
        final float indent;
        if (textAlign == TextAlignment.TA_JUSTIFY) {
            if (text.length() > 1) {
                indent = 0.0f;
            } else {
                indent = distributableSpace / 2; //Center if only one character
            }
        } else if (textAlign == TextAlignment.TA_CENTER) {
            indent = distributableSpace / 2;
        } else if (textAlign == TextAlignment.TA_RIGHT) {
            indent = distributableSpace;
        } else {
            indent = 0.0f;
        }
        Font oldFont = g2d.getFont();
        g2d.setFont(font);
        if (textAlign == TextAlignment.TA_JUSTIFY) {
            //move the individual glyphs
            for (int i = 0; i < gv.getNumGlyphs(); i++) {
                Point2D point = gv.getGlyphPosition(i);
                point.setLocation(point.getX() + i * intercharSpace, point.getY());
                gv.setGlyphPosition(i, point);
            }
        }
        g2d.drawGlyphVector(gv, (float)x1 + indent, (float)y1);
        g2d.setFont(oldFont);
    }

}
