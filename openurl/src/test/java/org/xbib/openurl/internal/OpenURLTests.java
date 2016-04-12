package org.xbib.openurl.internal;

import java.io.StringWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.xml.sax.InputSource;

import org.xbib.openurl.ContextObject;
import org.xbib.openurl.Format;
import org.xbib.openurl.OpenURLRequest;
import org.xbib.openurl.OpenURLRequestProcessor;
import org.xbib.openurl.OpenURLResponse;
import org.xbib.openurl.Transport;
import org.xbib.openurl.internal.serializers.XMLSerializer;
import org.xbib.openurl.internal.config.OpenURLConfigImpl;
import org.xbib.openurl.internal.parsers.XMLParser;

public class OpenURLTests {

    private static final Logger logger = LogManager.getLogger(OpenURLTests.class);

    @Test
    public void testRef() throws Exception {
        Map<String,String[]> map = new HashMap<>();
        map.put("url_ver", new String[] { "Z39.88-2004" });
        map.put("url_ctx_fmt", new String[] { "info:ofi/fmt:kev:mtx:ctx" } );
        map.put("svc_id", new String[] { "info:localhost/svc/HelloWorld" } );
        map.put("rft_id", new String [] {
            "info:doi/10.1126/science.275.5304.1320",
            "info:pmid/9036860"
        } );
        map.put("rfe_id", new String [] { "info:doi/10.1006/mthe.2000.0239" } );
        map.put("req_id", new String [] { "mailto:jane.doe@caltech.edu" } );
        map.put("ref_id", new String [] { "info:sid/elsevier.com:ScienceDirect" } );
        
        OpenURLConfigImpl config = new OpenURLConfigImpl();
        OpenURLRequestProcessor processor = config.getProcessor();
        logger.info("getProcessor=" + processor);
        List<Transport> transports = config.getTransports();
        for (Transport transport : transports) {
            logger.info("transport = " + transport);
            OpenURLRequest request = transport.toOpenURLRequest(map, false);
            logger.info("request = " + request);
            ContextObject[] contextobjects = request.getContextObjects();
            StringWriter sw = new StringWriter();
            XMLSerializer ser = new XMLSerializer(config);
            ser.serializeContextObjects(contextobjects, sw);
            logger.info("XML serialization = " + sw.toString());
            OpenURLResponse result = processor.resolve(request);
            logger.info("result after resolve = " + result);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            processor.write(out);
            logger.info("processor written output = " + out.toString());
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = schemaFactory.newSchema(getClass().getResource("/org/xbib/openurl/info-ofi-fmt-xml-xsd-ctx.xsd"));
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setSchema(schema);
            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.parse(new InputSource(new StringReader(sw.toString())));
            logger.info("XML CTX Schema parsing complete");
        }
    }

    @Test
    public void testByVal() throws Exception {
        Map<String,String[]> map = new HashMap<>();
        map.put("url_ver", new String[] { "Z39.88-2004" });
        map.put("url_ctx_fmt", new String[] { "info:ofi/fmt:kev:mtx:ctx" } );
        map.put("rft_val_fmt", new String[] { "info:ofi/fmt:kev:mtx:journal" });
        map.put("rft.atitle", new String[] { "Isolation of a common receptor for coxsackie B viruses and adenoviruses 2 and 5" });
        map.put("rft.jtitle", new String[] { "Science" });
        map.put("rft.aulast", new String[] { "Bergelson" });
        map.put("rft.auinit", new String[] { "J" });
        map.put("rft.date", new String[] { "1997" });
        map.put("rft.volume", new String[] { "275" });
        map.put("rft.spage", new String[] { "1320" });
        map.put("rft.epage", new String[] { "1323" });
        OpenURLConfigImpl config = new OpenURLConfigImpl();
        List<Transport> transports = config.getTransports();
        XMLSerializer ser = new XMLSerializer(config);
        XMLParser parser = new XMLParser(config);
        for (Transport transport : transports) {
            OpenURLRequest request = transport.toOpenURLRequest(map, false);
            for (ContextObject ctx : request.getContextObjects()) {
                StringWriter sw = new StringWriter();
                ser.serializeContextObjectReferent(ctx, sw);
                logger.info("XML serialization = " + sw.toString());
                ContextObject parsedctx = parser.createContextObject(Format.FORMAT_XML_JOURNAL_URI, 
                        new ByteArrayInputStream(sw.toString().getBytes()));
                logger.info("XML parser complete = " + parsedctx.getClass() + " context object = " + parsedctx );
            }
         }
    }

}