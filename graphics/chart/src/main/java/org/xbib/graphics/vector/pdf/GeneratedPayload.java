package org.xbib.graphics.vector.pdf;

import java.io.IOException;

public abstract class GeneratedPayload extends Payload {
    public GeneratedPayload(boolean stream) {
        super(stream);
    }

    @Override
    public byte[] getBytes() {
        try {
            for (byte b : generatePayload()) {
                super.write(b);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return super.getBytes();
    }

    @Override
    public void write(int b) throws IOException {
        throw new UnsupportedOperationException("Payload will be calculated and is read only.");
    }

    protected abstract byte[] generatePayload();
}

