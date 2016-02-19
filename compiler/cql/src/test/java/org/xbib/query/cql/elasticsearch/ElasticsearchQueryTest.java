package org.xbib.query.cql.elasticsearch;

import org.junit.Assert;
import org.junit.Test;
import org.xbib.query.cql.CQLParser;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

public class ElasticsearchQueryTest extends Assert {

    @Test
    public void testValidQueries() throws IOException {
        test("queries.txt");
    }

    @Test
    public void testSimpleTermFilter() throws Exception {
        String cql = "Jörg";
        CQLParser parser = new CQLParser(cql);
        parser.parse();
        ElasticsearchFilterGenerator generator = new ElasticsearchFilterGenerator();
        parser.getCQLQuery().accept(generator);
        String json = generator.getResult().string();
        //logger.info("{} --> {}", cql, json);
        assertEquals(json, "{\"term\":{\"cql.allIndexes\":\"Jörg\"}}");
    }

    @Test
    public void testFieldTermFilter() throws Exception {
        String cql = "dc.type = electronic";
        CQLParser parser = new CQLParser(cql);
        parser.parse();
        ElasticsearchFilterGenerator generator = new ElasticsearchFilterGenerator();
        parser.getCQLQuery().accept(generator);
        String json = generator.getResult().string();
        //logger.info("{} --> {}", cql, json);
        assertEquals(json, "{\"query\":{\"term\":{\"dc.type\":\"electronic\"}}}");
    }

    @Test
    public void testDoubleFieldTermFilter() throws Exception {
        String cql = "dc.type = electronic and dc.date = 2013";
        CQLParser parser = new CQLParser(cql);
        parser.parse();
        ElasticsearchFilterGenerator generator = new ElasticsearchFilterGenerator();
        parser.getCQLQuery().accept(generator);
        String json = generator.getResult().string();
        //logger.info("{} --> {}", cql, json);
        assertEquals(
                "{\"query\":{\"bool\":{\"must\":[{\"term\":{\"dc.type\":\"electronic\"}},{\"term\":{\"dc.date\":\"2013\"}}]}}}",
                json
        );
    }

    @Test
    public void testTripleFieldTermFilter() throws Exception {
        String cql = "dc.format = online and dc.type = electronic and dc.date = 2013";
        CQLParser parser = new CQLParser(cql);
        parser.parse();
        ElasticsearchFilterGenerator generator = new ElasticsearchFilterGenerator();
        parser.getCQLQuery().accept(generator);
        String json = generator.getResult().string();
        //logger.info("{} --> {}", cql, json);
        assertEquals(
                "{\"query\":{\"bool\":{\"must\":[{\"bool\":{\"must\":[{\"term\":{\"dc.format\":\"online\"}},{\"term\":{\"dc.type\":\"electronic\"}}]}},{\"term\":{\"dc.date\":\"2013\"}}]}}}",
                json);
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
                    if (pos > 0) {
                        validate(line.substring(0, pos), line.substring(pos+1));
                        ok++;
                    }
                } catch (Exception e) {
                   // logger.warn(e.getMessage(), e);
                    errors++;
                }
                count++;
            }
        }
        lr.close();
        assertEquals(0, errors);
        assertEquals(count, ok);
    }

    private void validate(String cql, String expected) throws Exception {
        CQLParser parser = new CQLParser(cql);
        parser.parse();
        //logger.info("success: cql={}", cql);
        ElasticsearchQueryGenerator generator = new ElasticsearchQueryGenerator();
        parser.getCQLQuery().accept(generator);
        String elasticsearchQuery = generator.getSourceResult();
        //logger.info("success: elastic={}", elasticsearchQuery);
        assertEquals(expected, elasticsearchQuery);
    }

}
