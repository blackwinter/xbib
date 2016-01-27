package org.xbib.io.archive.tar;

import org.xbib.io.Packet;
import org.xbib.io.Session;
import org.xbib.io.StreamCodecService;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.GZIPInputStream;

public class PerformanceTest {

    private final static Path path = Paths.get("/Users/joerg/import/hbz/vk/20140816/clob-20140815-20140816.tar.gz");

    public void test1() throws Exception {
        int buffersize = 4096;
        long t0 = System.nanoTime();
        InputStream in = Files.newInputStream(path);
        GZIPInputStream gzin = new GZIPInputStream(in, buffersize);
        long counter = 0L;
        TarArchiveInputStream tin = new TarArchiveInputStream(gzin);
        TarArchiveInputEntry entry;
        while ((entry = tin.getNextTarEntry()) != null) {
            int size = (int)entry.getEntrySize();
            byte[] b = new byte[size];
            counter += tin.read(b, 0, size);
        }
        long t1 = System.nanoTime();
        System.err.println("tar test1 delta c=" + counter + " t=" + (t1 - t0) / 1000000);
    }

    public void test2() throws Exception {
        int buffersize = 4096;
        long t0 = System.nanoTime();
        StreamCodecService codecFactory = StreamCodecService.getInstance();
        InputStream in = Files.newInputStream(path);
        InputStream gzin = codecFactory.getCodec("gz").decode(in, buffersize);
        long counter = 0L;
        TarArchiveInputStream tin = new TarArchiveInputStream(gzin);
        TarArchiveInputEntry entry;
        while ((entry = tin.getNextTarEntry()) != null) {
            int size = (int)entry.getEntrySize();
            byte[] b = new byte[size];
            counter += tin.read(b, 0, size);
        }
        long t1 = System.nanoTime();
        System.err.println("tar test2 delta c=" + counter + " t=" + (t1 - t0) / 1000000);
    }

    public void test3() throws Exception {
        int buffersize = 1024*1024;
        long t0 = System.nanoTime();
        StreamCodecService codecFactory = StreamCodecService.getInstance();
        InputStream in = Files.newInputStream(path);
        InputStream gzin = codecFactory.getCodec("gz").decode(in, buffersize);
        long counter = 0L;
        TarArchiveInputStream tin = new TarArchiveInputStream(gzin);
        TarArchiveInputEntry entry;
        while ((entry = tin.getNextTarEntry()) != null) {
            int size = (int)entry.getEntrySize();
            byte[] b = new byte[size];
            counter += tin.read(b, 0, size);
        }
        long t1 = System.nanoTime();
        System.err.println("tar test3 delta c=" + counter + " t=" + (t1 - t0) / 1000000);
    }

    public void test4() throws Exception {
        int buffersize = 1024*1024;
        long t0 = System.nanoTime();
        StreamCodecService codecFactory = StreamCodecService.getInstance();
        InputStream in = Files.newInputStream(path);
        InputStream gzin = codecFactory.getCodec("gz").decode(in, buffersize);
        long counter = 0L;
        TarArchiveInputStream tin = new TarArchiveInputStream(gzin);
        TarArchiveInputEntry entry;
        while ((entry = tin.getNextTarEntry()) != null) {
            int size = (int)entry.getEntrySize();
            byte[] b = new byte[size];
            counter += tin.read(b, 0, size);
        }
        long t1 = System.nanoTime();
        System.err.println("tar test4 delta c=" + counter + " t=" + (t1 - t0) / 1000000);
    }

    public void test5() throws Exception {
        long t0 = System.nanoTime();
        TarConnection c = new TarConnection(new URL("file:" + path));
        TarSession session = c.createSession();
        session.open(Session.Mode.READ);
        long counter = 0L;
        Packet message;
        while ((message = session.read()) != null) {
            String s = message.packet().toString();
            counter += s.length();
        }
        session.close();
        c.close();
        long t1 = System.nanoTime();
        System.err.println("tar test5 delta c=" + counter + " t=" + (t1 - t0) / 1000000);
    }

    public void test6() throws Exception {
        int buffersize = 65536;
        URI uri = URI.create("file:" + path);
        TarConnection c = new TarConnection(new URL("file:" + path));
        long t0 = System.nanoTime();
        TarSession session = c.createSession();
        session.setBufferSize(buffersize);
        session.open(Session.Mode.READ);
        long counter = 0L;
        Packet message;
        while ((message = session.read()) != null) {
            String s = message.packet().toString();
            counter += s.length();
        }
        session.close();
        c.close();
        long t1 = System.nanoTime();
        System.err.println("tar test6 delta c=" + counter + " t=" + (t1 - t0) / 1000000);
    }

    public void test7() throws Exception {
        long t0 = System.nanoTime();
        TarConnection c = new TarConnection(new URL("file:" + path));
        int buffersize = 65536;
        TarSession session = c.createSession();
        session.setBufferSize(buffersize);
        session.open(Session.Mode.READ);
        long counter = 0L;
        Packet message;
        while ((message = session.read()) != null) {
            String s = message.packet().toString();
            counter += s.length();
        }
        session.close();
        c.close();
        long t1 = System.nanoTime();
        System.err.println("tar test7 delta c=" + counter + " t=" + (t1 - t0) / 1000000);
    }

}
