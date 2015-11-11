package bench;

import org.junit.Test;
import org.xbib.marc.Field;
import org.xbib.marc.json.MarcXchangeJSONLinesReader;
import org.xbib.marc.json.MarcXchangeJSONLinesWriter;
import org.xbib.marc.xml.MarcXchangeContentHandler;
import org.xbib.marc.xml.MarcXchangeReader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;

import com.carrotsearch.junitbenchmarks.AbstractBenchmark;
import com.carrotsearch.junitbenchmarks.BenchmarkOptions;

public class BenchTest extends AbstractBenchmark {

    //@Test
    @BenchmarkOptions(benchmarkRounds = 20, warmupRounds = 5)
    public void test() throws Exception {
        deserialize(serialize());
    }

    private String serialize() throws IOException {
        InputStream in = getClass().getResource("topics.xml").openStream();
        MarcXchangeReader reader = new MarcXchangeReader(in);
        OutputStream out = new ByteArrayOutputStream();
        MarcXchangeJSONLinesWriter writer = new MarcXchangeJSONLinesWriter(out);
        reader.setMarcXchangeListener(writer);
        reader.parse();
        in.close();
        out.close();
        return out.toString();
    }

    private void deserialize(String s) throws IOException {
        StringReader stringReader = new StringReader(s);
        MarcXchangeJSONLinesReader marcXchangeJSONLinesReader = new MarcXchangeJSONLinesReader(stringReader, handler);
        marcXchangeJSONLinesReader.parse();
    }

    MyHandler handler = new MyHandler();

    class MyHandler extends MarcXchangeContentHandler {
        int count;

        @Override
        public void beginCollection() {
        }

        @Override
        public void endCollection() {
        }

        @Override
        public void beginRecord(String format, String type) {
            count++;
        }

        @Override
        public void leader(String label) {
        }

        @Override
        public void beginControlField(Field field) {
        }

        @Override
        public void endControlField(Field field) {
        }

        @Override
        public void beginDataField(Field field) {
        }

        @Override
        public void endDataField(Field field) {
        }

        @Override
        public void beginSubField(Field field) {
        }

        @Override
        public void endSubField(Field field) {
        }

        @Override
        public void endRecord() {
        }

    };

}
