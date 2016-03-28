package org.xbib.io;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.xbib.io.TestUtil.assertByteArraysEquals;
import static org.xbib.io.TestUtil.assertEquivalent;

public class ByteStringTest {
    private final String bronzeHorseman = "На берегу пустынных волн";

    @Test
    public void ofCopyRange() {
        byte[] bytes = "Hello, World!".getBytes(StandardCharsets.UTF_8);
        ByteString byteString = ByteString.of(bytes, 2, 9);
        // Verify that the bytes were copied out.
        bytes[4] = (byte) 'a';
        assertEquals("llo, Worl", byteString.utf8());
    }

    @Test
    public void getByte() throws Exception {
        ByteString byteString = ByteString.decodeHex("ab12");
        assertEquals(-85, byteString.getByte(0));
        assertEquals(18, byteString.getByte(1));
    }

    @Test
    public void getByteOutOfBounds() throws Exception {
        ByteString byteString = ByteString.decodeHex("ab12");
        try {
            byteString.getByte(2);
            fail();
        } catch (IndexOutOfBoundsException expected) {
        }
    }

    @Test
    public void equals() throws Exception {
        ByteString byteString = ByteString.decodeHex("000102");
        assertTrue(byteString.equals(byteString));
        assertTrue(byteString.equals(ByteString.decodeHex("000102")));
        assertTrue(ByteString.of().equals(ByteString.EMPTY));
        assertTrue(ByteString.EMPTY.equals(ByteString.of()));
        assertFalse(byteString.equals(new Object()));
        assertFalse(byteString.equals(ByteString.decodeHex("000201")));
    }

    @Test
    public void utf8() throws Exception {
        ByteString byteString = ByteString.encodeUtf8(bronzeHorseman);
        assertByteArraysEquals(byteString.toByteArray(), bronzeHorseman.getBytes(StandardCharsets.UTF_8));
        assertTrue(byteString.equals(ByteString.of(bronzeHorseman.getBytes(StandardCharsets.UTF_8))));
        assertEquals(byteString.utf8(), bronzeHorseman);
    }

    @Test
    public void md5() {
        assertEquals("6cd3556deb0da54bca060b4c39479839",
                ByteString.encodeUtf8("Hello, world!").md5().hex());
        assertEquals("c71dc6df4b2e434b8c74fd6dd6ca3f85",
                ByteString.encodeUtf8("One Two Three").md5().hex());
        assertEquals("37b69fb926e239e049d7e43987974b99",
                ByteString.encodeUtf8(bronzeHorseman).md5().hex());
    }

    @Test
    public void sha256() {
        assertEquals("315f5bdb76d078c43b8ac0064e4a0164612b1fce77c869345bfc94c75894edd3",
                ByteString.encodeUtf8("Hello, world!").sha256().hex());
        assertEquals("641e54ba5e49e169408148a25bef8ca8fa4f8aab222fe8ce4b3535a570ddd68e",
                ByteString.encodeUtf8("One Two Three").sha256().hex());
        assertEquals("4d869e1c3d94568a5344235d9e4f187b8d5d78d06c5c622854c669f2f582d33e",
                ByteString.encodeUtf8(bronzeHorseman).sha256().hex());
    }

    @Test
    public void testHashCode() throws Exception {
        ByteString byteString = ByteString.decodeHex("0102");
        assertEquals(byteString.hashCode(), byteString.hashCode());
        assertEquals(byteString.hashCode(), ByteString.decodeHex("0102").hashCode());
    }

    @Test
    public void read() throws Exception {
        InputStream in = new ByteArrayInputStream("abc".getBytes(StandardCharsets.UTF_8));
        assertEquals(ByteString.decodeHex("6162"), ByteString.read(in, 2));
        assertEquals(ByteString.decodeHex("63"), ByteString.read(in, 1));
        assertEquals(ByteString.of(), ByteString.read(in, 0));
    }

    @Test
    public void readAndToLowercase() throws Exception {
        InputStream in = new ByteArrayInputStream("ABC".getBytes(StandardCharsets.UTF_8));
        assertEquals(ByteString.encodeUtf8("ab"), ByteString.read(in, 2).toAsciiLowercase());
        assertEquals(ByteString.encodeUtf8("c"), ByteString.read(in, 1).toAsciiLowercase());
        assertEquals(ByteString.EMPTY, ByteString.read(in, 0).toAsciiLowercase());
    }

    @Test
    public void toAsciiLowerCaseNoUppercase() throws Exception {
        ByteString s = ByteString.encodeUtf8("a1_+");
        assertSame(s, s.toAsciiLowercase());
    }

    @Test
    public void toAsciiAllUppercase() throws Exception {
        assertEquals(ByteString.encodeUtf8("ab"), ByteString.encodeUtf8("AB").toAsciiLowercase());
    }

    @Test
    public void toAsciiStartsLowercaseEndsUppercase() throws Exception {
        assertEquals(ByteString.encodeUtf8("abcd"), ByteString.encodeUtf8("abCD").toAsciiLowercase());
    }

    @Test
    public void readAndToUppercase() throws Exception {
        InputStream in = new ByteArrayInputStream("abc".getBytes(StandardCharsets.UTF_8));
        assertEquals(ByteString.encodeUtf8("AB"), ByteString.read(in, 2).toAsciiUppercase());
        assertEquals(ByteString.encodeUtf8("C"), ByteString.read(in, 1).toAsciiUppercase());
        assertEquals(ByteString.EMPTY, ByteString.read(in, 0).toAsciiUppercase());
    }

    @Test
    public void toAsciiStartsUppercaseEndsLowercase() throws Exception {
        assertEquals(ByteString.encodeUtf8("ABCD"), ByteString.encodeUtf8("ABcd").toAsciiUppercase());
    }

    @Test
    public void substring() throws Exception {
        ByteString byteString = ByteString.encodeUtf8("Hello, World!");

        assertEquals(byteString.substring(0), byteString);
        assertEquals(byteString.substring(0, 5), ByteString.encodeUtf8("Hello"));
        assertEquals(byteString.substring(7), ByteString.encodeUtf8("World!"));
        assertEquals(byteString.substring(6, 6), ByteString.encodeUtf8(""));
    }

    @Test
    public void substringWithInvalidBounds() throws Exception {
        ByteString byteString = ByteString.encodeUtf8("Hello, World!");

        try {
            byteString.substring(-1);
            fail();
        } catch (IllegalArgumentException expected) {
        }

        try {
            byteString.substring(0, 14);
            fail();
        } catch (IllegalArgumentException expected) {
        }

        try {
            byteString.substring(8, 7);
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void write() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteString.decodeHex("616263").write(out);
        assertByteArraysEquals(new byte[]{0x61, 0x62, 0x63}, out.toByteArray());
    }

    @Test
    public void encodeHex() throws Exception {
        assertEquals("000102", ByteString.of((byte) 0x0, (byte) 0x1, (byte) 0x2).hex());
    }

    @Test
    public void decodeHex() throws Exception {
        assertEquals(ByteString.of((byte) 0x0, (byte) 0x1, (byte) 0x2), ByteString.decodeHex("000102"));
    }

    @Test
    public void decodeHexOddNumberOfChars() throws Exception {
        try {
            ByteString.decodeHex("aaa");
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void decodeHexInvalidChar() throws Exception {
        try {
            ByteString.decodeHex("a\u0000");
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void toStringOnEmptyByteString() {
        assertEquals("ByteString[size=0]", ByteString.of().toString());
    }

    @Test
    public void toStringOnSmallByteStringIncludesContents() {
        assertEquals("ByteString[size=16 data=a1b2c3d4e5f61a2b3c4d5e6f10203040]",
                ByteString.decodeHex("a1b2c3d4e5f61a2b3c4d5e6f10203040").toString());
    }

    @Test
    public void toStringOnLargeByteStringIncludesMd5() {
        assertEquals("ByteString[size=17 md5=2c9728a2138b2f25e9f89f99bdccf8db]",
                ByteString.encodeUtf8("12345678901234567").toString());
    }

    @Test
    public void javaSerializationTestNonEmpty() throws Exception {
        ByteString byteString = ByteString.encodeUtf8(bronzeHorseman);
        assertEquivalent(byteString, TestUtil.reserialize(byteString));
    }

    @Test
    public void javaSerializationTestEmpty() throws Exception {
        ByteString byteString = ByteString.of();
        assertEquivalent(byteString, TestUtil.reserialize(byteString));
    }

    @Test
    public void compareToSingleBytes() throws Exception {
        List<ByteString> originalByteStrings = Arrays.asList(
                ByteString.decodeHex("00"),
                ByteString.decodeHex("01"),
                ByteString.decodeHex("7e"),
                ByteString.decodeHex("7f"),
                ByteString.decodeHex("80"),
                ByteString.decodeHex("81"),
                ByteString.decodeHex("fe"),
                ByteString.decodeHex("ff"));

        List<ByteString> sortedByteStrings = new ArrayList<>(originalByteStrings);
        Collections.shuffle(sortedByteStrings, new Random(0));
        Collections.sort(sortedByteStrings);

        assertEquals(originalByteStrings, sortedByteStrings);
    }

    @Test
    public void compareToMultipleBytes() throws Exception {
        List<ByteString> originalByteStrings = Arrays.asList(
                ByteString.decodeHex(""),
                ByteString.decodeHex("00"),
                ByteString.decodeHex("0000"),
                ByteString.decodeHex("000000"),
                ByteString.decodeHex("00000000"),
                ByteString.decodeHex("0000000000"),
                ByteString.decodeHex("0000000001"),
                ByteString.decodeHex("000001"),
                ByteString.decodeHex("00007f"),
                ByteString.decodeHex("0000ff"),
                ByteString.decodeHex("000100"),
                ByteString.decodeHex("000101"),
                ByteString.decodeHex("007f00"),
                ByteString.decodeHex("00ff00"),
                ByteString.decodeHex("010000"),
                ByteString.decodeHex("010001"),
                ByteString.decodeHex("01007f"),
                ByteString.decodeHex("0100ff"),
                ByteString.decodeHex("010100"),
                ByteString.decodeHex("01010000"),
                ByteString.decodeHex("0101000000"),
                ByteString.decodeHex("0101000001"),
                ByteString.decodeHex("010101"),
                ByteString.decodeHex("7f0000"),
                ByteString.decodeHex("7f0000ffff"),
                ByteString.decodeHex("ffffff"));

        List<ByteString> sortedByteStrings = new ArrayList<>(originalByteStrings);
        Collections.shuffle(sortedByteStrings, new Random(0));
        Collections.sort(sortedByteStrings);

        assertEquals(originalByteStrings, sortedByteStrings);
    }

    @Test
    public void asByteBuffer() {
        assertEquals(0x42, ByteString.of((byte) 0x41, (byte) 0x42, (byte) 0x43).asByteBuffer().get(1));
    }
}
