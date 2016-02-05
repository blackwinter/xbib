package org.xbib.graphics.vector.visual;

import java.awt.*;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.List;

public class CharacterTest extends TestCase {
    public CharacterTest() throws IOException {
    }

    @Override
    public void draw(Graphics2D g) {
        double w = getPageSize().width;
        double h = getPageSize().height;

        Charset latin1 = Charset.forName("ISO-8859-1");
        CharsetEncoder latin1Encoder = latin1.newEncoder();
        List<String> charactersInCharset = new ArrayList<String>();
        for (char char_ = Character.MIN_VALUE; char_ < Character.MAX_VALUE; char_++) {
            String javaString = String.valueOf(char_);
            if (latin1Encoder.canEncode(char_)) {
                charactersInCharset.add(javaString);
            }
        }

        final int colCount = (int) Math.ceil(Math.sqrt(charactersInCharset.size()));
        final int rowCount = colCount;
        double tileWidth = w / colCount;
        double tileHeight = h / rowCount;
        int charIndex = 0;
        for (double y = 0.0; y < h; y += tileHeight) {
            for (double x = 0.0; x < w; x += tileWidth) {
                String c = charactersInCharset.get(charIndex);
                double tileCenterX = x + tileWidth / 2.0;
                double tileCenterY = y + tileHeight / 2.0;
                g.drawString(c, (float) tileCenterX, (float) tileCenterY);
                charIndex++;
            }
        }
    }
}
