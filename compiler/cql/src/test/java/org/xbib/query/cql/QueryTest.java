package org.xbib.query.cql;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

public class QueryTest extends Assert {

    private final static Logger logger = LogManager.getLogger(QueryTest.class);

    @Test
    public void testValidQueries() throws IOException {
        test("queries.txt");
    }

    private void test(String path) throws IOException {
        int count = 0;
        int ok = 0;
        int errors = 0;
        LineNumberReader lr = new LineNumberReader(new InputStreamReader(getClass().getResourceAsStream(path), "UTF-8"));
        String line;
        while ((line = lr.readLine()) != null) {
            if (line.trim().length() > 0 && !line.startsWith("#")) {
                try {
                    int pos = line.indexOf('|');
                    if (pos >0) {
                        validate(line.substring(0, pos), line.substring(pos+1));
                    } else {
                        validate(line);
                    }
                    ok++;
                } catch (Exception e) {
                    logger.warn(e.getMessage());
                    errors++;
                }
                count++;
            }
        }
        lr.close();
        assertEquals(errors, 0);
        assertEquals(ok, count);
    }

    private void validate(String line) throws Exception {
        CQLParser parser = new CQLParser(line);
        parser.parse();
        logger.info("{} ===> {}", line, parser.getCQLQuery());
        assertEquals(line, parser.getCQLQuery().toString());
    }

    private void validate(String line, String expected) throws Exception {
        CQLParser parser = new CQLParser(line);
        parser.parse();
        logger.info("{} ====> {}", line, expected);
        assertEquals(expected, parser.getCQLQuery().toString());
    }
}
