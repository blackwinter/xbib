package org.xbib.csv;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;

public class CSVParserTest {

    private final static Logger logger = LogManager.getLogger(CSVParserTest.class);

    @Test
    public void testCommaSeparated() throws IOException {
        InputStream in = getClass().getResourceAsStream("titleFile.csv");
        int count = 1;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            CSVParser csvParser = new CSVParser(reader);
            Iterator<List<String>> it = csvParser.iterator();
            while (it.hasNext()) {
                List<String> row = it.next();
                logger.info("count={} row={}", count, row);
                count++;
            }
        }
    }
}
