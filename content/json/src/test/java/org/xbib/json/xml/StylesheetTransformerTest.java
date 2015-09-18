package org.xbib.json.xml;

import org.testng.annotations.Test;
import org.xbib.xml.namespace.XmlNamespaceContext;
import org.xbib.xml.transform.StylesheetTransformer;
import org.xml.sax.InputSource;

import javax.xml.namespace.QName;
import javax.xml.transform.sax.SAXSource;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

public class StylesheetTransformerTest {

    private final QName root = new QName("http://example.org", "result", "ex");

    private XmlNamespaceContext context = XmlNamespaceContext.getDefaultInstance();

    @Test
    public void testJsonAsXML() throws Exception {
        InputStream in = getClass().getResourceAsStream("/org/xbib/json/xml/dc.json");
        if (in == null) {
            throw new IOException("dc.json not found");
        }
        context.addNamespace("xbib", "http://xbib.org/");
        context.addNamespace("bib", "info:bib");
        context.addNamespace("lia", "http://xbib.org/lia/");

        JsonXmlReader reader = new JsonXmlReader()
                .root(root)
                .context(context);
        File file = File.createTempFile("dc.", ".xml");
        FileWriter out = new FileWriter(file);
        StylesheetTransformer transformer = new StylesheetTransformer().setPath(
                "src/main/resources",
                "src/main/resources/xsl");
        // no style sheet, just a simple copy
        transformer.setSource(new SAXSource(reader, new InputSource(in)))
                .setResult(out)
                .transform();
        out.close();
        transformer.close();
    }
}
