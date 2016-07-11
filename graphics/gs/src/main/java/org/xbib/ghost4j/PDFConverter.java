package org.ghost4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * PDF converter
 */
public class PDFConverter {

    public static final int OPTION_AUTOROTATEPAGES_NONE = 0;
    public static final int OPTION_AUTOROTATEPAGES_ALL = 1;
    public static final int OPTION_AUTOROTATEPAGES_PAGEBYPAGE = 2;
    public static final int OPTION_AUTOROTATEPAGES_OFF = 3;

    public static final int OPTION_PROCESSCOLORMODEL_RGB = 0;
    public static final int OPTION_PROCESSCOLORMODEL_GRAY = 1;
    public static final int OPTION_PROCESSCOLORMODEL_CMYK = 2;

    public static final int OPTION_PDFSETTINGS_DEFAULT = 0;
    public static final int OPTION_PDFSETTINGS_SCREEN = 1;
    public static final int OPTION_PDFSETTINGS_EBOOK = 2;
    public static final int OPTION_PDFSETTINGS_PRINTER = 3;
    public static final int OPTION_PDFSETTINGS_PREPRESS = 4;

    /**
     * Define auto rotate pages behaviour. Can be OPTION_AUTOROTATEPAGES_NONE,
     * OPTION_AUTOROTATEPAGES_ALL, OPTION_AUTOROTATEPAGES_PAGEBYPAGE or
     * OPTION_AUTOROTATEPAGES_OFF (default).
     */
    private int autoRotatePages = OPTION_AUTOROTATEPAGES_OFF;

    /**
     * Define process color model. Can be OPTION_PROCESSCOLORMODEL_RGB,
     * OPTION_PROCESSCOLORMODEL_GRAY or OPTION_PROCESSCOLORMODEL_CMYK.
     */
    private int processColorModel;

    /**
     * Define PDF settings to use. Can be OPTION_PDFSETTINGS_DEFAULT,
     * OPTION_PDFSETTINGS_SCREEN, OPTION_PDFSETTINGS_EBOOK,
     * OPTION_PDFSETTINGS_PRINTER or OPTION_PDFSETTINGS_PREPRESS.
     */
    private int PDFSettings;

    /**
     * Define PDF version compatibility level (default is "1.4").
     */
    private String compatibilityLevel = "1.4";

    /**
     * Enable PDFX generation (default is false).
     */
    private boolean PDFX = false;

    /**
     * Define standard paper size for the generated PDF file. This parameter is
     * ignored if a paper size is provided in the input file. Default value is
     * "letter".
     */
    private PaperSize paperSize = PaperSize.A4;

    /**
     * Run method called to perform the actual process of the converter.
     *
     * @param inputStream the input document
     * @param outputStream output stream
     * @throws IOException
     */
    public void convert(InputStream inputStream, OutputStream outputStream)
            throws IOException, GhostscriptException {
        if (outputStream == null) {
            return;
        }
        Ghostscript gs = Ghostscript.getInstance();
        int argCount = 15;
        if (autoRotatePages != OPTION_AUTOROTATEPAGES_OFF) {
            argCount++;
        }
        String[] gsArgs = new String[argCount];
        gsArgs[0] = "-ps2pdf";
        gsArgs[1] = "-dNOPAUSE";
        gsArgs[2] = "-dBATCH";
        gsArgs[3] = "-dSAFER";
        int paramPosition = 3;
        switch (autoRotatePages) {
            case OPTION_AUTOROTATEPAGES_NONE:
                paramPosition++;
                gsArgs[paramPosition] = "-dAutoRotatePages=/None";
                break;
            case OPTION_AUTOROTATEPAGES_ALL:
                paramPosition++;
                gsArgs[paramPosition] = "-dAutoRotatePages=/All";
                break;
            case OPTION_AUTOROTATEPAGES_PAGEBYPAGE:
                paramPosition++;
                gsArgs[paramPosition] = "-dAutoRotatePages=/PageByPage";
                break;
            default:
                break;
        }
        paramPosition++;
        switch (processColorModel) {
            case OPTION_PROCESSCOLORMODEL_CMYK:
                gsArgs[paramPosition] = "-dProcessColorModel=/DeviceCMYK";
                break;
            case OPTION_PROCESSCOLORMODEL_GRAY:
                gsArgs[paramPosition] = "-dProcessColorModel=/DeviceGray";
                break;
            default:
                gsArgs[paramPosition] = "-dProcessColorModel=/DeviceRGB";
        }
        paramPosition++;
        switch (PDFSettings) {
            case OPTION_PDFSETTINGS_EBOOK:
                gsArgs[paramPosition] = "-dPDFSETTINGS=/ebook";
                break;
            case OPTION_PDFSETTINGS_SCREEN:
                gsArgs[paramPosition] = "-dPDFSETTINGS=/screen";
                break;
            case OPTION_PDFSETTINGS_PRINTER:
                gsArgs[paramPosition] = "-dPDFSETTINGS=/printer";
                break;
            case OPTION_PDFSETTINGS_PREPRESS:
                gsArgs[paramPosition] = "-dPDFSETTINGS=/prepress";
                break;
            default:
                gsArgs[paramPosition] = "-dPDFSETTINGS=/default";
        }
        paramPosition++;
        gsArgs[paramPosition] = "-dCompatibilityLevel=" + compatibilityLevel;
        paramPosition++;
        gsArgs[paramPosition] = "-dPDFX=" + PDFX;
        paramPosition++;
        gsArgs[paramPosition] = "-dDEVICEWIDTHPOINTS=" + paperSize.getWidth();
        paramPosition++;
        gsArgs[paramPosition] = "-dDEVICEHEIGHTPOINTS=" + paperSize.getHeight();
        paramPosition++;
        gsArgs[paramPosition] = "-sDEVICE=pdfwrite";
        paramPosition++;
        Path output = Files.createTempFile("pdf", "pdf");
        gsArgs[paramPosition] = "-sOutputFile=" + output.toAbsolutePath().toString();
        paramPosition++;
        gsArgs[paramPosition] = "-q";
        paramPosition++;
        gsArgs[paramPosition] = "-f";
        paramPosition++;
        gsArgs[paramPosition] = "-";
        try {
            gs.setStdIn(inputStream);
            gs.initialize(gsArgs);
            byte[] content = Files.readAllBytes(output);
            outputStream.write(content);
        } finally {
            try {
                Ghostscript.deleteInstance();
            } catch (GhostscriptException e) {
                throw new IOException(e);
            }
            Files.delete(output);
        }
    }

    public int getAutoRotatePages() {
        return autoRotatePages;
    }

    public void setAutoRotatePages(int autoRotatePages) {
        this.autoRotatePages = autoRotatePages;
    }

    public int getProcessColorModel() {
        return processColorModel;
    }

    public void setProcessColorModel(int processColorModel) {
        this.processColorModel = processColorModel;
    }

    public String getCompatibilityLevel() {
        return compatibilityLevel;
    }

    public void setCompatibilityLevel(String compatibilityLevel) {
        this.compatibilityLevel = compatibilityLevel;
    }

    public int getPDFSettings() {
        return PDFSettings;
    }

    public void setPDFSettings(int PDFSettings) {
        this.PDFSettings = PDFSettings;
    }

    public boolean isPDFX() {
        return PDFX;
    }

    public void setPDFX(boolean PDFX) {
        this.PDFX = PDFX;
    }

    public PaperSize getPaperSize() {
        return paperSize;
    }

    public void setPaperSize(PaperSize paperSize) {
        this.paperSize = paperSize;
    }

    public void setPaperSize(String paperSizeName) {

        PaperSize found = PaperSize.getStandardPaperSize(paperSizeName);
        if (found != null) {
            this.setPaperSize(found);
        }
    }

}
