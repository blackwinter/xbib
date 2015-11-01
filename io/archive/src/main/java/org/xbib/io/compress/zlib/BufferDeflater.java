package org.xbib.io.compress.zlib;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.zip.CRC32;
import java.util.zip.Deflater;

public class BufferDeflater {


    /**
     * The standard 10 byte GZIP header
     */
    private static final byte[] GZIP_HEADER = new byte[]{0x1f, (byte) 0x8b,
            Deflater.DEFLATED, 0, 0, 0, 0, 0, 0, 0};

    /**
     * CRC-32 of uncompressed data.
     */
    private final CRC32 crc = new CRC32();

    /**
     * Deflater to deflate data
     */
    private final Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION,
            true);

    /**
     * Output buffer building area
     */
    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    /**
     * Internal transfer space
     */
    private final byte[] transfer = new byte[1000];

    /**
     * The flush mode to use at the end of each buffer
     */
    private final int flushMode;


    /**
     * New buffer deflater
     *
     * @param syncFlush if true, all data in buffer can be immediately decompressed
     *                  from output buffer
     */
    public BufferDeflater(boolean syncFlush) {
        flushMode = syncFlush ? Deflater.SYNC_FLUSH : Deflater.NO_FLUSH;
        buffer.write(GZIP_HEADER, 0, GZIP_HEADER.length);
    }


    /**
     * Deflate the buffer
     *
     * @param in the buffer to deflate
     * @return deflated representation of the buffer
     */
    public ByteBuffer deflate(ByteBuffer in) {
        byte[] inBytes;
        int off = in.position();
        int len = in.remaining();
        if (in.hasArray()) {
            inBytes = in.array();
        } else {
            off = 0;
            inBytes = new byte[len];
            in.get(inBytes);
        }
        crc.update(inBytes, off, len);
        deflater.setInput(inBytes, off, len);
        while (!deflater.needsInput()) {
            int r = deflater.deflate(transfer, 0, transfer.length, flushMode);
            buffer.write(transfer, 0, r);
        }
        byte[] outBytes = buffer.toByteArray();
        buffer.reset();
        return ByteBuffer.wrap(outBytes);
    }

    /**
     * Write the final buffer. This writes any remaining compressed data and the GZIP trailer.
     *
     * @return the final buffer
     */
    public ByteBuffer doFinal() {
        deflater.finish();
        int r;
        do {
            r = deflater.deflate(transfer, 0, transfer.length, Deflater.FULL_FLUSH);
            buffer.write(transfer, 0, r);
        } while (r == transfer.length);
        writeInt((int) crc.getValue());
        writeInt((int) deflater.getBytesRead());
        deflater.reset();
        byte[] outBytes = buffer.toByteArray();
        buffer.reset();
        return ByteBuffer.wrap(outBytes);
    }

    /**
     * Write a 32 bit value in little-endian order
     *
     * @param v the value to write
     */
    private void writeInt(int v) {
        buffer.write(v & 0xff);
        buffer.write((v >> 8) & 0xff);
        buffer.write((v >> 16) & 0xff);
        buffer.write((v >> 24) & 0xff);
    }

    /**
     * For testing. Pass in the name of a file to GZIP compress
     * @param args
     * @throws IOException
     */
    /*public static void main(String[] args) throws IOException {
        File inFile = new File(args[0]);
        File outFile = new File(args[0]+".test.gz");
        FileChannel inChan = (new FileInputStream(inFile)).getChannel();
        FileChannel outChan = (new FileOutputStream(outFile)).getChannel();

        BufferDeflate2 def = new BufferDeflate2(false);

        ByteBuffer buf = ByteBuffer.allocate(500);
        while( true ) {
            buf.clear();
            int r = inChan.read(buf);
            if( r==-1 ) break;
            buf.flip();
            ByteBuffer compBuf = def.deflate(buf);
            outChan.write(compBuf);
        }

        ByteBuffer compBuf = def.doFinal();
        outChan.write(compBuf);

        inChan.close();
        outChan.close();
    }*/
}