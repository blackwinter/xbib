package org.xbib.openurl.internal.parsers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.xbib.openurl.ContextObject;
import org.xbib.openurl.Format;
import org.xbib.openurl.internal.config.OpenURLConfigImpl;
import java.io.InputStream;

public class XMLStreamParserTests {

    private final static Logger logger = LogManager.getLogger(XMLStreamParserTests.class);

    @Test
    public void testContextObject() throws Exception {
        OpenURLConfigImpl config = new OpenURLConfigImpl();
        XMLStreamParser parser = new XMLStreamParser(config);
        InputStream in = getClass().getResourceAsStream("/org/xbib/openurl/ContextObject.xml");
        ContextObject ctxobj = parser.createContextObject(Format.FORMAT_XML_CONTEXT_URI, in);
        logger.info("XML parser complete, context object = {}", ctxobj );
    }
}
