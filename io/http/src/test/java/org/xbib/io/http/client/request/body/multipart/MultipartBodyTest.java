package org.xbib.io.http.client.request.body.multipart;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpHeaders;
import org.apache.commons.io.IOUtils;
import org.testng.annotations.Test;
import org.xbib.io.http.client.request.body.Body.BodyState;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class MultipartBodyTest {

    private static File getTestfile() throws URISyntaxException {
        final ClassLoader cl = MultipartBodyTest.class.getClassLoader();
        final URL url = cl.getResource("textfile.txt");
        assertNotNull(url);
        return new File(url.toURI());
    }

    private static void compareContentLength(final List<Part> parts) throws IOException {
        assertNotNull(parts);
        // get expected values
        final MultipartBody multipartBody = MultipartUtils.newMultipartBody(parts, HttpHeaders.EMPTY_HEADERS);
        final long expectedContentLength = multipartBody.getContentLength();
        try {
            final ByteBuf buffer = Unpooled.buffer(8192);
            while (multipartBody.transferTo(buffer) != BodyState.STOP) {
            }
            assertEquals(buffer.readableBytes(), expectedContentLength);
        } finally {
            IOUtils.closeQuietly(multipartBody);
        }
    }

    @Test(groups = "standalone")
    public void testBasics() throws Exception {
        final List<Part> parts = new ArrayList<>();

        // add a file
        final File testFile = getTestfile();
        System.err.println(testFile.length());
        parts.add(new FilePart("filePart", testFile));

        // add a byte array
        parts.add(new ByteArrayPart("baPart", "testMultiPart".getBytes(UTF_8), "application/test", UTF_8, "fileName"));

        // add a string
        parts.add(new StringPart("stringPart", "testString"));

        compareContentLength(parts);
    }
}
