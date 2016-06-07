package org.xbib.marc.transformer;

import org.xbib.charset.ByteToCharUSM94;

import java.io.CharConversionException;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;

public class Marc8StringTransformer implements StringTransformer {

    private ByteToCharUSM94 byteToCharUSM94 = new ByteToCharUSM94();

    @Override
    public String transform(String value) {
        byte[] b = value.getBytes(StandardCharsets.ISO_8859_1);
        char[] ch = new char[value.length()];
        try {
            byteToCharUSM94.convert(b, 0, b.length, ch, 0, ch.length);
        } catch (ArrayIndexOutOfBoundsException e) {
            return "???";
        } catch (CharConversionException e) {
            return "???";
        }
        String str = new String(ch);
        return Normalizer.normalize(str, Normalizer.Form.NFKC);
    }
}
