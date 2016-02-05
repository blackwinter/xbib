package org.xbib.graphics.vector;

import org.xbib.graphics.vector.intermediate.CommandHandler;

import java.io.IOException;
import java.io.OutputStream;

public interface Document extends CommandHandler {
    void write(OutputStream out) throws IOException;

    void close();
}

