package org.xbib.io.compress.gzip;

import org.testng.annotations.Test;
import org.xbib.io.StreamCodecService;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.zip.GZIPInputStream;

public class PerformanceTest {

    private final static String path = "/Users/joerg/import/hbz/vk/20140816/clob-20140815-20140816.tar.gz";

    public void test1() throws Exception {
        int buffersize = 4096;
        long t0 = System.nanoTime();
        FileInputStream in = new FileInputStream(path);
        GZIPInputStream gzin = new GZIPInputStream(in, buffersize);
        long counter = 0L;
        byte[] b = new byte[buffersize];
        while (true){
            int num = gzin.read(b, 0, buffersize);
            if (num == -1) {
                break;
            }
            counter += num;
        }
        gzin.close();
        long t1 = System.nanoTime();
        System.err.println("gzip test1 c=" + counter + " t=" + (t1 - t0) / 1000000);
    }

    public void test2() throws Exception {
        int buffersize = 65536;
        long t0 = System.nanoTime();
        FileInputStream in = new FileInputStream(path);
        GZIPInputStream gzin = new GZIPInputStream(in, buffersize);
        long counter = 0L;
        byte[] b = new byte[buffersize];
        while (true){
            int num = gzin.read(b, 0, buffersize);
            if (num == -1) {
                break;
            }
            counter += num;
        }
        gzin.close();
        long t1 = System.nanoTime();
        System.err.println("gzip test2 c=" + counter + " t=" + (t1 - t0) / 1000000);
    }

    public void test3() throws Exception {
        int buffersize = 4096;
        long t0 = System.nanoTime();
        StreamCodecService codecFactory = StreamCodecService.getInstance();
        FileInputStream in = new FileInputStream(path);
        InputStream gzin = codecFactory.getCodec("gz").decode(in, buffersize);
        ReadableByteChannel fileChannel = Channels.newChannel(gzin);
        ByteBuffer buffer = ByteBuffer.allocate(buffersize);
        int bytes = fileChannel.read(buffer);
        long counter = 0L;
        while (bytes != -1) {
            buffer.flip();
            while (buffer.hasRemaining()) {
                buffer.get();
                counter++;
            }
            buffer.clear();
            bytes = fileChannel.read(buffer);
        }
        fileChannel.close();
        gzin.close();
        long t1 = System.nanoTime();
        System.err.println("gzip test3 c=" + counter + " t=" + (t1 - t0) / 1000000);
    }

    public void test4() throws Exception {
        int buffersize = 65536;
        long t0 = System.nanoTime();
        StreamCodecService codecFactory = StreamCodecService.getInstance();
        FileInputStream in = new FileInputStream(path);
        InputStream gzin = codecFactory.getCodec("gz").decode(in, buffersize);
        ReadableByteChannel fileChannel = Channels.newChannel(gzin);
        ByteBuffer buffer = ByteBuffer.allocate(buffersize);
        int bytes = fileChannel.read(buffer);
        long counter = 0L;
        while (bytes != -1) {
            buffer.flip();
            while (buffer.hasRemaining()) {
                buffer.get();
                counter++;
            }
            buffer.clear();
            bytes = fileChannel.read(buffer);
        }
        fileChannel.close();
        gzin.close();
        long t1 = System.nanoTime();
        System.err.println("gzip test4 c=" + counter + " t=" + (t1 - t0) / 1000000);
    }

}
