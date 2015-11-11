
package org.xbib.io.compress.zlib;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ZlibTest extends Assert {

    @Test
    public void helloWorld() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ZOutputStream zOut = new ZOutputStream(out, ZConstants.Z_BEST_COMPRESSION, false);
        ObjectOutputStream objOut = new ObjectOutputStream(zOut);
        String helloWorld = "Hello World!";
        objOut.writeObject(helloWorld);
        zOut.close();
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        ZInputStream zIn = new ZInputStream(in);
        ObjectInputStream objIn = new ObjectInputStream(zIn);
        assertEquals("Hello World!", objIn.readObject());
    }
    private final static byte[] hello = "hello, hello! ".getBytes();


    static {
        hello[hello.length - 1] = 0;
    }

    private void checkError(ZStream z, int err, String msg) throws Exception {
        if (err != ZConstants.Z_OK) {
            throw new RuntimeException(msg + " " + z.msg + " error: " + err);
        }
    }

    @Test
    public void testDeflateInflate() throws Exception {
        int err;
        int comprLen = 40000;
        int uncomprLen = comprLen;
        byte[] compr = new byte[comprLen];
        byte[] uncompr = new byte[uncomprLen];

        ZStream stream = new ZStream();

        err = stream.deflateInit(ZConstants.Z_DEFAULT_COMPRESSION);
        checkError(stream, err, "deflateInit");

        stream.nextin = hello;
        stream.nextinindex = 0;

        stream.nextout = compr;
        stream.nextoutindex = 0;

        while (stream.totalin != hello.length &&
                stream.totalout < comprLen) {
            stream.availin = stream.availout = 1; // force small buffers
            err = stream.deflate(ZConstants.Z_NO_FLUSH);
            checkError(stream, err, "deflate");
        }

        while (true) {
            stream.availout = 1;
            err = stream.deflate(ZConstants.Z_FINISH);
            if (err == ZConstants.Z_STREAM_END) {
                break;
            }
            checkError(stream, err, "deflate");
        }

        err = stream.deflateEnd();
        checkError(stream, err, "deflateEnd");

        ZStream d_stream = new ZStream();

        d_stream.nextin = compr;
        d_stream.nextinindex = 0;
        d_stream.nextout = uncompr;
        d_stream.nextoutindex = 0;

        err = d_stream.inflateInit();
        checkError(d_stream, err, "inflateInit");

        while (d_stream.totalout < uncomprLen &&
                d_stream.totalin < comprLen) {
            d_stream.availin = d_stream.availout = 1; /* force small buffers */
            err = d_stream.inflate(ZConstants.Z_NO_FLUSH);
            if (err == ZConstants.Z_STREAM_END) {
                break;
            }
            checkError(d_stream, err, "inflate");
        }

        err = d_stream.inflateEnd();
        checkError(d_stream, err, "inflateEnd");

        int i = 0;
        for (; i < hello.length; i++) {
            if (hello[i] == 0) {
                break;
            }
        }
        int j = 0;
        for (; j < uncompr.length; j++) {
            if (uncompr[j] == 0) {
                break;
            }
        }

        if (i == j) {
            for (i = 0; i < j; i++) {
                if (hello[i] != uncompr[i]) {
                    break;
                }
            }
            if (i == j) {
                return;
            }
        } else {
            throw new RuntimeException("bad inflate");
        }
    }
    private final static byte[] dictionary = "hello ".getBytes();


    static {
        dictionary[dictionary.length - 1] = 0;
    }

    @Test
    public void testDictDeflateInflate() throws Exception {
        int err;
        int comprLen = 40000;
        int uncomprLen = comprLen;
        byte[] uncompr = new byte[uncomprLen];
        byte[] compr = new byte[comprLen];
        long dictId;

        ZStream stream = new ZStream();
        err = stream.deflateInit(ZConstants.Z_BEST_COMPRESSION);
        checkError(stream, err, "deflateInit");

        err = stream.deflateSetDictionary(dictionary, dictionary.length);
        checkError(stream, err, "deflateSetDictionary");

        dictId = stream.adler;

        stream.nextout = compr;
        stream.nextoutindex = 0;
        stream.availout = comprLen;

        stream.nextin = hello;
        stream.nextinindex = 0;
        stream.availin = hello.length;

        err = stream.deflate(ZConstants.Z_FINISH);
        if (err != ZConstants.Z_STREAM_END) {
            throw new RuntimeException("deflate should report Z_STREAM_END");
        }
        err = stream.deflateEnd();
        checkError(stream, err, "deflateEnd");

        ZStream d_stream = new ZStream();

        d_stream.nextin = compr;
        d_stream.nextinindex = 0;
        d_stream.availin = comprLen;

        err = d_stream.inflateInit();
        checkError(d_stream, err, "inflateInit");
        d_stream.nextout = uncompr;
        d_stream.nextoutindex = 0;
        d_stream.availout = uncomprLen;

        while (true) {
            err = d_stream.inflate(ZConstants.Z_NO_FLUSH);
            if (err == ZConstants.Z_STREAM_END) {
                break;
            }
            if (err == ZConstants.Z_NEED_DICT) {
                if ((int) d_stream.adler != (int) dictId) {
                    throw new RuntimeException("unexpected dictionary");
                }
                err = d_stream.inflateSetDictionary(dictionary, dictionary.length);
            }
            checkError(d_stream, err, "inflate with dict");
        }

        err = d_stream.inflateEnd();
        checkError(d_stream, err, "inflateEnd");

        int j = 0;
        for (; j < uncompr.length; j++) {
            if (uncompr[j] == 0) {
                break;
            }
        }
    }

    @Test
    public void testFlushSync() throws Exception {
        int err;
        int comprLen = 40000;
        int uncomprLen = comprLen;
        byte[] compr = new byte[comprLen];
        byte[] uncompr = new byte[uncomprLen];
        int len = hello.length;

        ZStream stream = new ZStream();

        err = stream.deflateInit(ZConstants.Z_DEFAULT_COMPRESSION);
        checkError(stream, err, "deflate");

        stream.nextin = hello;
        stream.nextinindex = 0;
        stream.nextout = compr;
        stream.nextoutindex = 0;
        stream.availin = 3;
        stream.availout = comprLen;

        err = stream.deflate(ZConstants.Z_FULL_FLUSH);
        checkError(stream, err, "deflate");

        compr[3]++;              // force an error in first compressed block
        stream.availin = len - 3;

        err = stream.deflate(ZConstants.Z_FINISH);
        if (err != ZConstants.Z_STREAM_END) {
            checkError(stream, err, "deflate");
        }
        err = stream.deflateEnd();
        checkError(stream, err, "deflateEnd");
        comprLen = (int) (stream.totalout);

        ZStream d_stream = new ZStream();

        d_stream.nextin = compr;
        d_stream.nextinindex = 0;
        d_stream.availin = 2;

        err = d_stream.inflateInit();
        checkError(d_stream, err, "inflateInit");
        d_stream.nextout = uncompr;
        d_stream.nextoutindex = 0;
        d_stream.availout = uncomprLen;

        err = d_stream.inflate(ZConstants.Z_NO_FLUSH);
        checkError(d_stream, err, "inflate");

        d_stream.availin = comprLen - 2;

        err = d_stream.inflateSync();
        checkError(d_stream, err, "inflateSync");

        err = d_stream.inflate(ZConstants.Z_FINISH);
        if (err != ZConstants.Z_DATA_ERROR) {
            throw new RuntimeException("inflate should report DATA_ERROR");
        }

        err = d_stream.inflateEnd();
        checkError(d_stream, err, "inflateEnd");

        int j = 0;
        for (; j < uncompr.length; j++) {
            if (uncompr[j] == 0) {
                break;
            }
        }
    }

    @Test
    public void testLargeDeflateInflate() throws Exception {
        int err;
        int comprLen = 40000;
        int uncomprLen = comprLen;
        byte[] compr = new byte[comprLen];
        byte[] uncompr = new byte[uncomprLen];

        ZStream stream = new ZStream();

        err = stream.deflateInit(ZConstants.Z_BEST_SPEED);
        checkError(stream, err, "deflateInit");

        stream.nextout = compr;
        stream.nextoutindex = 0;
        stream.availout = comprLen;

        // At this point, uncompr is still mostly zeroes, so it should compress
        // very well:
        stream.nextin = uncompr;
        stream.availin = uncomprLen;
        err = stream.deflate(ZConstants.Z_NO_FLUSH);
        checkError(stream, err, "deflate");
        if (stream.availin != 0) {
            throw new RuntimeException("deflate not greedy");
        }

        // Feed in already compressed data and switch to no compression:
        stream.deflateParams(ZConstants.Z_NO_COMPRESSION, ZConstants.Z_DEFAULT_STRATEGY);
        stream.nextin = compr;
        stream.nextinindex = 0;
        stream.availin = comprLen / 2;
        err = stream.deflate(ZConstants.Z_NO_FLUSH);
        checkError(stream, err, "deflate");

        // Switch back to compressing mode:
        stream.deflateParams(ZConstants.Z_BEST_COMPRESSION, ZConstants.Z_FILTERED);
        stream.nextin = uncompr;
        stream.nextinindex = 0;
        stream.availin = uncomprLen;
        err = stream.deflate(ZConstants.Z_NO_FLUSH);
        checkError(stream, err, "deflate");

        err = stream.deflate(ZConstants.Z_FINISH);
        if (err != ZConstants.Z_STREAM_END) {
            throw new RuntimeException("deflate should report Z_STREAM_END");
        }
        err = stream.deflateEnd();
        checkError(stream, err, "deflateEnd");

        ZStream d_stream = new ZStream();

        d_stream.nextin = compr;
        d_stream.nextinindex = 0;
        d_stream.availin = comprLen;

        err = d_stream.inflateInit();
        checkError(d_stream, err, "inflateInit");

        while (true) {
            d_stream.nextout = uncompr;
            d_stream.nextoutindex = 0;
            d_stream.availout = uncomprLen;
            err = d_stream.inflate(ZConstants.Z_NO_FLUSH);
            if (err == ZConstants.Z_STREAM_END) {
                break;
            }
            checkError(d_stream, err, "inflate large");
        }

        err = d_stream.inflateEnd();
        checkError(d_stream, err, "inflateEnd");

        if (d_stream.totalout != 2 * uncomprLen + comprLen / 2) {
            throw new RuntimeException("bad large inflate: " + d_stream.totalout);
        }
    }
}
