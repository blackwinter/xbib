package org.xbib.graphics.vector.pdf;

import org.xbib.graphics.vector.util.FlateEncodeStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class Payload extends OutputStream {
    private final ByteArrayOutputStream byteStream;
    private final boolean stream;
    private OutputStream filteredStream;
    private boolean empty;

    public Payload(boolean stream) {
        byteStream = new ByteArrayOutputStream();
        filteredStream = byteStream;
        this.stream = stream;
        empty = true;
    }

    public byte[] getBytes() {
        return byteStream.toByteArray();
    }

    public boolean isStream() {
        return stream;
    }

    @Override
    public void write(int b) throws IOException {
        filteredStream.write(b);
        empty = false;
    }

    @Override
    public void close() throws IOException {
        super.close();
        filteredStream.close();
    }

    public void addFilter(Class<FlateEncodeStream> filterClass) {
        if (!empty) {
            throw new IllegalStateException("Cannot add filter after writing to payload.");
        }
        try {
            // TODO
            filteredStream = filterClass.getConstructor(OutputStream.class)
                    .newInstance(filteredStream);
        } catch (Exception e) {
            //
        }
    }
}

