package org.ghost4j;

import java.awt.Image;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class SimpleRenderer {

    public static final int OPTION_ANTIALIASING_NONE = 0;
    public static final int OPTION_ANTIALIASING_LOW = 1;
    public static final int OPTION_ANTIALIASING_MEDIUM = 2;
    public static final int OPTION_ANTIALIASING_HIGH = 4;

    private int antialiasing = OPTION_ANTIALIASING_HIGH;

    private int resolution = 300;

    public List<Image> render(Path inputPath, int begin, int end)
            throws IOException {
        Ghostscript gs = Ghostscript.getInstance();
        ImageWriterDisplayCallback displayCallback = new ImageWriterDisplayCallback();
        String[] gsArgs = {"-dQUIET", "-dNOPAUSE", "-dBATCH", "-dSAFER",
                "-dFirstPage=" + (begin + 1),
                "-dLastPage=" + (end + 1),
                "-sDEVICE=display", "-sDisplayHandle=0",
                "-dDisplayFormat=16#804", "-r" + this.getResolution()};

        if (this.antialiasing != OPTION_ANTIALIASING_NONE) {
            gsArgs = Arrays.copyOf(gsArgs, gsArgs.length + 2);
            gsArgs[gsArgs.length - 2] = "-dTextAlphaBits=" + this.antialiasing;
            gsArgs[gsArgs.length - 1] = "-dGraphicsAlphaBits="
                    + this.antialiasing;
        }
        gsArgs = Arrays.copyOf(gsArgs, gsArgs.length + 2);
        gsArgs[gsArgs.length - 2] = "-f";
        gsArgs[gsArgs.length - 1] = inputPath.toAbsolutePath().toString();
        try {
            gs.setDisplayCallback(displayCallback);
            gs.initialize(gsArgs);
            gs.exit();
        } catch (GhostscriptException e) {
            throw new IOException(e);
        } finally {
            try {
                Ghostscript.deleteInstance();
            } catch (GhostscriptException e) {
                throw new IOException(e);
            }
        }
        return displayCallback.getImages();
    }

    public int getAntialiasing() {
        return antialiasing;
    }

    public void setAntialiasing(int antialiasing) {
        this.antialiasing = antialiasing;
    }

    public int getResolution() {
        return resolution;
    }

    public void setResolution(int resolution) {
        this.resolution = resolution;
    }
}
