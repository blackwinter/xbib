package org.xbib.charset;

import java.io.CharConversionException;
import java.io.UnsupportedEncodingException;

public abstract class LocalByteConverter {

    public static LocalByteConverter getConverter(String encName)
            throws UnsupportedEncodingException {
        Class c;
        LocalByteConverter lbc;
        String name = getConverterClassName(encName);

        try {
            c = Class.forName(name);
        } catch (ClassNotFoundException e) {
            throw new UnsupportedEncodingException(name);
        }

        try {
            lbc = (LocalByteConverter) c.newInstance();
        } catch (InstantiationException e) {
            throw new UnsupportedEncodingException(name);
        } catch (IllegalAccessException e) {
            throw new UnsupportedEncodingException(name);
        }

        return lbc;
    }

    private static String getConverterClassName(String encName) {
        if (encName.indexOf('.') < 0) {
            return "ORG.oclc.LocalByteConverter.ByteToChar" + encName;
        }

        return encName;
    }

    public static LocalByteConverter getDefault()
            throws UnsupportedEncodingException {
        return getConverter("USM94");
    }

    public abstract int convert(byte byteBuf[], int byteOffset, int byteLen,
                                char charBuf[], int charOffset, int charLen)
            throws CharConversionException;

    public char[] convertAll(byte byteBuf[]) throws CharConversionException {
        int len = byteBuf.length, newLen = len * getMaxCharsPerByte();
        char charBuf[] = new char[newLen];
        int i = convert(byteBuf, 0, byteBuf.length, charBuf, 0, newLen);
        char newBuf[] = new char[i];
        System.arraycopy(charBuf, 0, newBuf, 0, i);
        return newBuf;
    }

    public int getMaxCharsPerByte() {
        return 1;
    }

    public abstract void setIgnoreRecoverableErrors(boolean val);
}


// END OF LocalByteConverter