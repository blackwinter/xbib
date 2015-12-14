package org.xbib.io.http.client.request.body.generators;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.testng.annotations.Test;
import org.xbib.io.http.client.request.body.Body;
import org.xbib.io.http.client.request.body.Body.BodyState;
import org.xbib.io.http.client.request.body.generator.ByteArrayBodyGenerator;

import java.io.IOException;
import java.util.Random;

import static org.testng.Assert.assertEquals;

public class ByteArrayBodyGeneratorTest {

    private final Random random = new Random();
    private final int chunkSize = 1024 * 8;

    @Test(groups = "standalone")
    public void testSingleRead() throws IOException {
        final int srcArraySize = chunkSize - 1;
        final byte[] srcArray = new byte[srcArraySize];
        random.nextBytes(srcArray);

        final ByteArrayBodyGenerator babGen =
                new ByteArrayBodyGenerator(srcArray);
        final Body body = babGen.createBody();

        final ByteBuf chunkBuffer = Unpooled.buffer(chunkSize);

        // should take 1 read to get through the srcArray
        body.transferTo(chunkBuffer);
        assertEquals(chunkBuffer.readableBytes(), srcArraySize, "bytes read");
        chunkBuffer.clear();

        assertEquals(body.transferTo(chunkBuffer), BodyState.STOP, "body at EOF");
    }

    @Test(groups = "standalone")
    public void testMultipleReads() throws IOException {
        final int srcArraySize = (3 * chunkSize) + 42;
        final byte[] srcArray = new byte[srcArraySize];
        random.nextBytes(srcArray);

        final ByteArrayBodyGenerator babGen =
                new ByteArrayBodyGenerator(srcArray);
        final Body body = babGen.createBody();

        final ByteBuf chunkBuffer = Unpooled.buffer(chunkSize);

        int reads = 0;
        int bytesRead = 0;
        while (body.transferTo(chunkBuffer) != BodyState.STOP) {
            reads += 1;
            bytesRead += chunkBuffer.readableBytes();
            chunkBuffer.clear();
        }
        assertEquals(reads, 4, "reads to drain generator");
        assertEquals(bytesRead, srcArraySize, "bytes read");
    }
}
