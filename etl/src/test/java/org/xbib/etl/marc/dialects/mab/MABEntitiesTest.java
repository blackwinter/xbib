package org.xbib.etl.marc.dialects.mab;

import org.junit.Test;
import org.xbib.marc.Iso2709Reader;
import org.xbib.marc.MarcXchange2KeyValue;
import org.xbib.rdf.RdfContentBuilder;
import org.xml.sax.InputSource;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import static org.xbib.rdf.content.RdfXContentFactory.rdfXContentBuilder;

public class MABEntitiesTest {

    @Test
    public void testSetupOfMABElements() throws Exception {
        MyQueue queue = new MyQueue();
        queue.execute();
        File file = File.createTempFile("mab-hbz-tit-elements", ".json");
        file.deleteOnExit();
        Writer writer = new FileWriter(file);
        queue.specification().dump("org/xbib/analyzer/mab/titel.json", writer);
        MarcXchange2KeyValue kv = new MarcXchange2KeyValue().addListener(queue);
        Iso2709Reader reader = new Iso2709Reader(null).setMarcXchangeListener(kv);
        reader.setProperty(Iso2709Reader.FORMAT, "MAB");
        reader.setProperty(Iso2709Reader.TYPE, "Titel");
        queue.close();
    }

    @Test
    public void testZDBMABElements() throws Exception {
        InputStream in = getClass().getResourceAsStream("1217zdbtit.dat");
        BufferedReader br = new BufferedReader(new InputStreamReader(in, "x-MAB"));
        File file = File.createTempFile("zdb-mab-titel",".xml");
        file.deleteOnExit();
        Writer w = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
        final Set<String> unmapped = Collections.synchronizedSet(new TreeSet<String>());
        MyQueue queue = new MyQueue();
        queue.setUnmappedKeyListener((id,key) -> unmapped.add("\"" + key + "\""));
        queue.execute();
        MarcXchange2KeyValue kv = new MarcXchange2KeyValue().addListener(queue);
        Iso2709Reader reader = new Iso2709Reader(br).setMarcXchangeListener(kv);
        reader.setProperty(Iso2709Reader.FORMAT, "MAB");
        reader.setProperty(Iso2709Reader.TYPE, "Titel");
        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer();
        InputSource source = new InputSource(br);
        StreamResult target = new StreamResult(w);
        transformer.transform(new SAXSource(reader, source), target);
        //logger.info("unknown ZDB MAB elements = {}", unmapped);
        queue.close();
    }

    class MyQueue extends MABEntityQueue {

        public MyQueue() {
            super("org.xbib.analyzer.mab.titel",
                  Runtime.getRuntime().availableProcessors(),
                  "org/xbib/analyzer/mab/titel.json");
        }

        @Override
        public void afterCompletion(MABEntityBuilderState state) throws IOException {
            // write title resource
            RdfContentBuilder builder = rdfXContentBuilder();
            builder.receive(state.getResource());
            //logger.info(builder.string());
        }
    }
}
