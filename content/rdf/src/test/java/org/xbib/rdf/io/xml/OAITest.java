package org.xbib.rdf.io.xml;

import org.testng.annotations.Test;
import org.xbib.helper.StreamTester;
import org.xbib.iri.IRI;
import org.xbib.iri.namespace.IRINamespaceContext;
import org.xbib.rdf.RdfContentBuilder;
import org.xbib.rdf.io.turtle.TurtleContentParams;
import org.xbib.text.CharUtils;
import org.xbib.text.UrlEncoding;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.InputStream;

import static org.xbib.rdf.RdfContentFactory.turtleBuilder;

public class OAITest extends StreamTester {

    @Test
    public void testOAIListRecords() throws Exception {
        String filename = "/org/xbib/rdf/io/xml/oai-listrecords.xml";
        InputStream in = getClass().getResourceAsStream(filename);
        if (in == null) {
            throw new IOException("file " + filename + " not found");
        }

        IRINamespaceContext context = IRINamespaceContext.newInstance();
        XmlContentParams params = new XmlContentParams(context);
        XmlHandler xmlHandler = new AbstractXmlResourceHandler(params) {

            @Override
            public boolean isResourceDelimiter(QName name) {
                return "oai_dc".equals(name.getLocalPart());
            }

            @Override
            public void identify(QName name, String value, IRI identifier) {
                if ("identifier".equals(name.getLocalPart()) && identifier == null) {
                    // make sure we can build an opaque IRI, whatever is out there
                    String s = UrlEncoding.encode(value, CharUtils.Profile.SCHEMESPECIFICPART.filter());
                    getResource().id(IRI.create("id:" + s));
                }
            }

            @Override
            public boolean skip(QName name) {
                return name.getLocalPart().startsWith("@");
            }

            @Override
            public XmlHandler setNamespaceContext(IRINamespaceContext namespaceContext) {
                return this;
            }

            @Override
            public IRINamespaceContext getNamespaceContext() {
                return context;
            }
        };
        TurtleContentParams turtleParams = new TurtleContentParams(context, true);
        RdfContentBuilder builder = turtleBuilder(turtleParams);
        xmlHandler.setBuilder(builder)
                .setNamespaceContext(context)
                .setDefaultNamespace("oai", "http://www.openarchives.org/OAI/2.0/oai_dc/");
        XmlContentParser parser = new XmlContentParser(in);
        parser.builder(builder);
        parser.setHandler(xmlHandler)
                .parse();
        assertStream(getClass().getResource("oai.ttl").openStream(), builder.streamInput());
    }
}
