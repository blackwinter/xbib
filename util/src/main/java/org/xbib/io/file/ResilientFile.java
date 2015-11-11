package org.xbib.io.file;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.concurrent.locks.ReentrantLock;

public class ResilientFile implements Closeable, Appendable {

    private final static Charset UTF8 = Charset.forName("UTF-8");

    private final ReentrantLock lock = new ReentrantLock();

    private File directory;

    private String name;

    private File file;

    private RandomAccessFile randomAccessFile;

    private FileChannel channel;

    private MappedByteBuffer buffer;

    private CharBuffer charBuffer;

    private int buffersize = 4096;

    private long position;

    public ResilientFile setDirectory(File directory) {
        this.directory = directory;
        return this;
    }

    public ResilientFile setName() {
        this.name = name;
        return this;
    }

    public ResilientFile setBufferSize(int buffersize) {
        this.buffersize = buffersize;
        return this;
    }

    public ResilientFile open() throws IOException {
        try {
            lock.lock();
            file = new File(directory, name);
            randomAccessFile = new RandomAccessFile(file, "rw");
            channel = randomAccessFile.getChannel();
            buffer = channel.map(FileChannel.MapMode.READ_WRITE, 0,
                    buffersize == 0 ? file.length() : buffersize);
            charBuffer = buffer.asCharBuffer();
            this.position = buffer.getLong();
            int len = buffer.getInt();
            if (len > 0) {
                byte[] b = new byte[len];
                buffer.get(b);
            }
            buffer.position(0);
        } finally {
            lock.unlock();
        }
        return this;
    }

    public void flush() throws IOException {
        buffer.force();
    }

    @Override
    public void close() throws IOException {
        try {
            lock.lock();
            if (buffer != null) {
                buffer.clear();
            }
            if (channel != null) {
                channel.close();
            }
            if (randomAccessFile != null) {
                randomAccessFile.close();
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Appendable append(CharSequence csq) throws IOException {
        charBuffer.append(csq);
        return this;
    }

    @Override
    public Appendable append(CharSequence csq, int start, int end) throws IOException {
        charBuffer.append(csq, start, end);
        return this;
    }

    @Override
    public Appendable append(char c) throws IOException {
        charBuffer.append(c);
        return this;
    }

    public Appendable append(ByteBuffer b) throws IOException {
        channel.write(b);
        return this;
    }

    public Appendable append(byte b) throws IOException {
        append(ByteBuffer.wrap(new byte[]{b}));
        return this;
    }

    public Appendable append(byte[] b, int start, int end) throws IOException {
        append(ByteBuffer.wrap(b, start, end));
        return this;
    }

}
