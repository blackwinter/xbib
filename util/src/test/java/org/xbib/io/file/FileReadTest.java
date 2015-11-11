package org.xbib.io.file;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class FileReadTest {

    private final Logger logger = LogManager.getLogger(FileReadTest.class.getName());

    private String name = "test.zip";

    private int SIZE = 8192;

    private int BIGSIZE = 1024 * 1024;

    public void test02() throws IOException {
        FileInputStream f = new FileInputStream(name);
        byte[] barray = new byte[SIZE];
        long checkSum = 0L;
        int nRead;
        while ((nRead = f.read(barray, 0, SIZE)) != -1) {
            for (int i = 0; i < nRead; i++) {
                checkSum += barray[i];
            }
        }
        f.close();
        logger.info("test 2 done {}", checkSum);
    }

    public void test03() throws IOException {
        // slow (too many read() ops)
        BufferedInputStream f = new BufferedInputStream(new FileInputStream(name));
        int b;
        long checkSum = 0L;
        while ((b = f.read()) != -1) {
            checkSum += b;
        }
        f.close();
        logger.info("test 3 done {}", checkSum);
    }


    public void test04() throws IOException {
        BufferedInputStream f = new BufferedInputStream(new FileInputStream(name));
        byte[] barray = new byte[SIZE];
        long checkSum = 0L;
        int nRead;
        while ((nRead = f.read(barray, 0, SIZE)) != -1) {
            for (int i = 0; i < nRead; i++) {
                checkSum += barray[i];
            }
        }
        f.close();
        logger.info("test 4 done {}", checkSum);
    }


    public void test06() throws IOException {
        RandomAccessFile f = new RandomAccessFile(name, "r");
        byte[] barray = new byte[SIZE];
        long checkSum = 0L;
        int nRead;
        while ((nRead = f.read(barray, 0, SIZE)) != -1) {
            for (int i = 0; i < nRead; i++) {
                checkSum += barray[i];
            }
        }
        f.close();
        logger.info("test 6 done {}", checkSum);
    }


    public void test07() throws IOException {
        // > 1 sec
        FileInputStream f = new FileInputStream(name);
        FileChannel ch = f.getChannel();
        ByteBuffer bb = ByteBuffer.allocate(SIZE);
        long checkSum = 0L;
        int nRead;
        while ((nRead = ch.read(bb)) != -1) {
            if (nRead == 0) {
                continue;
            }
            bb.position(0);
            bb.limit(nRead);
            while (bb.hasRemaining()) {
                checkSum += bb.get();
            }
            bb.clear();
        }
        f.close();
        logger.info("test 7 done {}", checkSum);
    }


    public void test08() throws IOException {
        FileInputStream f = new FileInputStream(name);
        FileChannel ch = f.getChannel();
        ByteBuffer bb = ByteBuffer.allocate(BIGSIZE);
        byte[] barray = new byte[SIZE];
        long checkSum = 0L;
        int nRead, nGet;
        while ((nRead = ch.read(bb)) != -1) {
            if (nRead == 0) {
                continue;
            }
            bb.position(0);
            bb.limit(nRead);
            while (bb.hasRemaining()) {
                nGet = Math.min(bb.remaining(), SIZE);
                bb.get(barray, 0, nGet);
                for (int i = 0; i < nGet; i++) {
                    checkSum += barray[i];
                }
            }
            bb.clear();
        }
        f.close();
        logger.info("test 8 done {}", checkSum);
    }


    public void test09() throws IOException {
        FileInputStream f = new FileInputStream(name);
        FileChannel ch = f.getChannel();
        byte[] barray = new byte[SIZE];
        ByteBuffer bb = ByteBuffer.wrap(barray);
        long checkSum = 0L;
        int nRead;
        while ((nRead = ch.read(bb)) != -1) {
            for (int i = 0; i < nRead; i++) {
                checkSum += barray[i];
            }
            bb.clear();
        }
        f.close();
        logger.info("test 9 done {}", checkSum);
    }

    public void test10() throws IOException {
        // > 1 sec
        FileInputStream f = new FileInputStream(name);
        FileChannel ch = f.getChannel();
        ByteBuffer bb = ByteBuffer.allocateDirect(SIZE);
        long checkSum = 0L;
        int nRead;
        while ((nRead = ch.read(bb)) != -1) {
            bb.position(0);
            bb.limit(nRead);
            while (bb.hasRemaining()) {
                checkSum += bb.get();
            }
            bb.clear();
        }
        f.close();
        logger.info("test 10 done {}", checkSum);
    }


    public void test11() throws IOException {
        FileInputStream f = new FileInputStream(name);
        FileChannel ch = f.getChannel();
        ByteBuffer bb = ByteBuffer.allocateDirect(BIGSIZE);
        byte[] barray = new byte[SIZE];
        long checkSum = 0L;
        int nRead, nGet;
        while ((nRead = ch.read(bb)) != -1) {
            if (nRead == 0) {
                continue;
            }
            bb.position(0);
            bb.limit(nRead);
            while (bb.hasRemaining()) {
                nGet = Math.min(bb.remaining(), SIZE);
                bb.get(barray, 0, nGet);
                for (int i = 0; i < nGet; i++) {
                    checkSum += barray[i];
                }
            }
            bb.clear();
        }
        f.close();
        logger.info("test 11 done {}", checkSum);
    }

    public void test14() throws IOException {
        FileInputStream f = new FileInputStream(name);
        MappedByteBufferInputStream in = new MappedByteBufferInputStream(f.getChannel());
        byte[] barray = new byte[SIZE];
        long checkSum = 0L;
        int nRead;
        while ((nRead = in.read(barray, 0, SIZE)) != -1) {
            for (int i = 0; i < nRead; i++) {
                checkSum += barray[i];
            }
        }
        f.close();
        logger.info("test 14 done {}", checkSum);
    }
}
