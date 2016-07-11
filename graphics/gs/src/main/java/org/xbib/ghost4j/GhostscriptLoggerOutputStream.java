package org.ghost4j;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Class used to wrap Ghostscript interpreter log messages in Log4J messages.
 */
public class GhostscriptLoggerOutputStream extends OutputStream {

    private static final String LOGGER_NAME = Ghostscript.class.getName();

    private static final int LINE_END = (int) '\n';

    private ByteArrayOutputStream baos = new ByteArrayOutputStream();

    private Logger logger;

    private Level level;

    public GhostscriptLoggerOutputStream(Level level) {
        logger = LogManager.getLogger(LOGGER_NAME);
        baos = new ByteArrayOutputStream();
        this.level = level;
    }

    public void write(int b) throws IOException {
        if (b == LINE_END) {
            logger.log(level, baos.toString());
            baos.reset();
        } else {
            baos.write(b);
        }
    }
}
