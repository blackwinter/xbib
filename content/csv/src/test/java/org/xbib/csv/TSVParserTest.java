package org.xbib.csv;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

public class TSVParserTest {

    private final static Logger logger = LogManager.getLogger("test");

    @Test
    public void testTabSeparated() throws IOException {
        InputStream in = getClass().getResourceAsStream("2076831-X-web.txt");
        InputStreamReader r = new InputStreamReader(in, "UTF-8");
        BufferedReader reader = new BufferedReader(r);
        // skip 3 lines
        reader.readLine();
        reader.readLine();
        reader.readLine();
        String line;
        while ((line = reader.readLine()) != null) {
            String[] s = line.split("\\t");
            //logger.info("len={} line={}", s.length, Arrays.asList(s));
            int i = 0;
            String sigel = i < s.length ? s[i++] : "";
            String isil = i < s.length ? s[i++] : "";
            String name = i < s.length ? s[i++] : ""; // unused
            String code1 = i < s.length ? s[i++] : "";
            String code2 = i < s.length ? s[i++] : "";
            String code3 = i < s.length ? s[i++] : "";
            String comment = i < s.length ? s[i++] : "";
            String firstDate = i < s.length ? s[i++] : "";
            String firstVolume = i < s.length ? s[i++] : "";
            String firstIssue = i < s.length ? s[i++] : "";
            String lastDate = i < s.length ? s[i++] : "";
            String lastVolume = i < s.length ? s[i++] : "";
            String lastIssue = i < s.length ? s[i++] : "";
            String movingWall = i < s.length ? s[i] : "";
            //logger.info("lastDate={}", lastDate);
        }

    }
}
