package org.xbib.rdf.io.sink;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;

/**
 * Implementation of {@link org.xbib.rdf.io.sink.CharSink}. Provides bridging to Java IO APIs
 * ({@link java.io.Writer}, {@link java.io.OutputStream}, {@link java.io.File}).
 */
public final class CharOutputSink implements CharSink {

    private File file;

    private Writer writer;

    private OutputStream outputStream;

    private boolean closeOnEndStream;

    private final Charset charset;

    private static final short BATCH_SIZE = 256;

    private StringBuilder buffer;

    private short bufferSize;

    /**
     * Creates class instance with default charset encoding..
     */
    public CharOutputSink() {
        this(Charset.defaultCharset());
    }

    /**
     * Creates class instance with specified charset encoding.
     *
     * @param charset charset
     */
    public CharOutputSink(Charset charset) {
        this.charset = charset;
    }

    /**
     * Redirects output to specified file
     *
     * @param file output file
     */
    public void connect(File file) {
        this.file = file;
        this.writer = null;
        this.outputStream = null;
        this.closeOnEndStream = true;
    }

    /**
     * Redirects output to specified writer
     *
     * @param writer output writer
     */
    public void connect(Writer writer) {
        this.file = null;
        this.writer = writer;
        this.outputStream = null;
        this.closeOnEndStream = false;
    }

    /**
     * Redirects output to specified stream
     *
     * @param outputStream output stream
     */
    public void connect(OutputStream outputStream) {
        this.file = null;
        this.writer = null;
        this.outputStream = outputStream;
        this.closeOnEndStream = false;
    }

    @Override
    public CharOutputSink process(String str) throws IOException {
        buffer.append(str);
        bufferSize += str.length();
        writeBuffer();
        return this;
    }

    @Override
    public CharOutputSink process(char ch) throws IOException {
        buffer.append(ch);
        bufferSize++;
        writeBuffer();
        return this;
    }

    @Override
    public CharOutputSink process(char[] buffer, int start, int count) throws IOException {
        this.buffer.append(buffer, start, count);
        bufferSize += count;
        writeBuffer();
        return this;
    }

    private void writeBuffer() throws IOException {
        if (bufferSize >= BATCH_SIZE) {
            writer.write(buffer.toString());
            buffer = new StringBuilder(BATCH_SIZE);
            bufferSize = 0;
        }
    }

    @Override
    public void setBaseUri(String baseUri) {
    }

    @Override
    public void startStream() throws IOException {
        buffer = new StringBuilder();
        bufferSize = 0;
        if (writer == null) {
            if (file != null) {
                try {
                    writer = new OutputStreamWriter(new FileOutputStream(file), charset);
                } catch (FileNotFoundException e) {
                    throw new IOException(e);
                }
            } else if (outputStream != null) {
                writer = new OutputStreamWriter(outputStream, charset);
            }
        }
    }

    @Override
    public void endStream() throws IOException {
        buffer.append("\n");
        bufferSize = BATCH_SIZE;
        writeBuffer();
        writer.flush();
        if (closeOnEndStream) {
            writer.close();
            writer = null;
        } else if (outputStream != null) {
            outputStream.close();
            outputStream = null;
        }
    }

    @Override
    public void beginDocument(String id) throws IOException {

    }

    @Override
    public void endDocument(String id) throws IOException {

    }

}