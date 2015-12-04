
package org.xbib.graphics.barcode.tools;

/**
 * Defines MIME types used in Barcode4J.
 */
public class MimeTypes {

    /** SVG MIME type: image/svg+xml */
    public static final String MIME_SVG  = "image/svg+xml";
    /** EPS MIME type: image/x-eps */
    public static final String MIME_EPS  = "image/x-eps";
    /** TIFF MIME type: image/tiff */
    public static final String MIME_TIFF = "image/tiff";
    /** JPEG MIME type: image/jpeg */
    public static final String MIME_JPEG = "image/jpeg";
    /** PNG MIME type: image/x-png */
    public static final String MIME_PNG  = "image/x-png";
    /** GIF MIME type: image/gif */
    public static final String MIME_GIF  = "image/gif";
    /** BMP MIME type: image/bmp*/
    public static final String MIME_BMP  = "image/bmp";

    private static final String[][] FORMAT_MAPPINGS =
            {{"svg", MIME_SVG},
             {"eps", MIME_EPS},
             {"image/eps", MIME_EPS},
             {"tif", MIME_TIFF},
             {"tiff", MIME_TIFF},
             {"jpg", MIME_JPEG},
             {"jpeg", MIME_JPEG},
             {"png", MIME_PNG},
             {"image/png", MIME_PNG},
             {"gif", MIME_GIF},
             {"image/x-bmp", MIME_BMP},
             {"bmp", MIME_BMP}};

    /**
     * Utility class: Constructor prevents instantiating when subclassed.
     */
    protected MimeTypes() {
        throw new UnsupportedOperationException();
    }

    /**
     * Converts a short format name, such as "svg" or "eps", to its MIME type,
     * if necessary. Known and unknown MIME types are passed through.
     * @param format short format name or MIME type
     * @return MIME type
     */
    public static String expandFormat(String format) {
        if (format == null || format.length() == 0) {
            return null;
        }
        for (int i = 0; i < FORMAT_MAPPINGS.length; i++) {
            if (format.equalsIgnoreCase(FORMAT_MAPPINGS[i][0])
                || format.equals(FORMAT_MAPPINGS[i][1])) {
                return FORMAT_MAPPINGS[i][1];
            }
        }
        return format.toLowerCase();
    }

    /**
     * Indicates whether a format is a bitmap format.
     * @param format short format name or MIME type
     * @return true if format is a bitmap format
     */
    public static boolean isBitmapFormat(String format) {
        String fmt = expandFormat(format);
        if (fmt == null) {
            return false;
        }
        return (fmt.equals(MIME_JPEG)
            || fmt.equals(MIME_TIFF)
            || fmt.equals(MIME_PNG)
            || fmt.equals(MIME_GIF)
            || fmt.equals(MIME_BMP));
    }

}
