package org.xbib.io.field;

import java.io.IOException;
import java.io.Reader;

public class CarriageReturnLineFeedStreamReader extends BufferedFieldStreamReader {
    private static final char LF = '\n';
    private static final char CR = '\r';

    public CarriageReturnLineFeedStreamReader(Reader reader)
    {
        super(reader);
    }

    @Override
    public String readLine() throws IOException {
        StringBuilder sb = new StringBuilder();
        int ch;
        boolean b = false;
        while((ch = read()) != -1) {
            if (b && ch == LF) {
                return sb.substring(0, sb.length()-1);
            }
            b = ch == CR;
            sb.append((char) ch);
        }
        String string = sb.toString();
        if (string.length() == 0) {
            return null;
        }
        return string;
    }
}