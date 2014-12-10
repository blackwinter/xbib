package org.xbib.entities.marc.dialects.mab;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.Test;
import org.xbib.io.InputService;
import org.xbib.marc.Iso2709Reader;
import org.xbib.marc.keyvalue.MarcXchange2KeyValue;
import org.xbib.rdf.RdfContentBuilder;
import org.xml.sax.InputSource;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import static org.xbib.rdf.content.RdfXContentFactory.rdfXContentBuilder;

public class MABEntitiesTest {

    private static final Logger logger = LogManager.getLogger(MABEntitiesTest.class.getName());

    @Test
    public void testSetupOfMABElements() throws Exception {
        MyQueue queue = new MyQueue();
        queue.execute();
        Writer writer = new FileWriter("mab-hbz-tit-elements.json");
        queue.specification().dump("org/xbib/analyzer/mab/titel.json", writer);
        MarcXchange2KeyValue kv = new MarcXchange2KeyValue().addListener(queue);
        Iso2709Reader reader = new Iso2709Reader(null).setMarcXchangeListener(kv);
        reader.setProperty(Iso2709Reader.FORMAT, "MAB");
        reader.setProperty(Iso2709Reader.TYPE, "Titel");
        queue.close();
    }

    @Test
    public void testZDBMABElements() throws Exception {
        URI uri = getClass().getResource("1217zdbtit.dat").toURI();
        InputStream in = InputService.getInputStream(uri);
        BufferedReader br = new BufferedReader(new InputStreamReader(in, "x-MAB"));
        Writer w = new OutputStreamWriter(new FileOutputStream("zdb-mab-titel.xml"), "UTF-8");
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
        logger.info("unknown ZDB MAB elements = {}", unmapped);
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
            //RdfXContentParams params = new RdfXContentParams(IRINamespaceContext.getInstance(), true);
            RdfContentBuilder builder = rdfXContentBuilder();
            builder.receive(state.getResource());
            logger.info(builder.string());
        }
    }
}
