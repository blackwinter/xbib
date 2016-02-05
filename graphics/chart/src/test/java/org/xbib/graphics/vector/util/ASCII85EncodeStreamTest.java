package org.xbib.graphics.vector.util;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static org.junit.Assert.assertEquals;

public class ASCII85EncodeStreamTest {

    private static void assertEncodedString(String expected, String input) throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(input.getBytes());

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        OutputStream encodeStream = new ASCII85EncodeStream(outStream);

        byte[] buffer = new byte[1024];
        for (int count = inputStream.read(buffer); count >= 0; count = inputStream.read(buffer)) {
            encodeStream.write(buffer, 0, count);
        }
        encodeStream.close();

        String encoded = outStream.toString("ISO-8859-1");

        assertEquals(expected, encoded);
    }

    @Test
    public void testEncoding() throws IOException {
        String input =
                "Man is distinguished, not only by his reason, but by this singular passion " +
                        "from other animals, which is a lust of the mind, that by a perseverance of " +
                        "delight in the continued and indefatigable generation of knowledge, exceeds " +
                        "the short vehemence of any carnal pleasure.";

        String expected =
                "9jqo^BlbD-BleB1DJ+*+F(f,q/0JhKF<GL>Cj@.4Gp$d7F!,L7@<6@)/0JDEF<G%<+EV:2F!,O<DJ+*." +
                        "@<*K0@<6L(Df-\\0Ec5e;DffZ(EZee.Bl.9pF\"AGXBPCsi+DGm>@3BB/F*&OCAfu2/AKYi(DIb:@FD,*)" +
                        "+C]U=@3BN#EcYf8ATD3s@q?d$AftVqCh[NqF<G:8+EV:.+Cf>-FD5W8ARlolDIal(DId<j@<?3r@:F%a" +
                        "+D58'ATD4$Bl@l3De:,-DJs`8ARoFb/0JMK@qB4^F!,R<AKZ&-DfTqBG%G>uD.RTpAKYo'+CT/5+Cei#" +
                        "DII?(E,9)oF*2M7/c~>";

        assertEncodedString(expected, input);
    }

    @Test
    public void testPadding() throws IOException {
        assertEncodedString("/c~>", ".");
    }

    @Test
    public void testEmpty() throws IOException {
        assertEncodedString("~>", "");
    }

}

