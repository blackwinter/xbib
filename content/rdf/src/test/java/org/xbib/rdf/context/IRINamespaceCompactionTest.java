
package org.xbib.rdf.context;

import org.junit.Assert;
import org.junit.Test;
import org.xbib.iri.IRI;
import org.xbib.iri.namespace.IRINamespaceContext;

public class IRINamespaceCompactionTest extends Assert {

    @Test
    public void testCompaction() throws Exception {
        IRINamespaceContext context = IRINamespaceContext.getInstance();
        assertEquals(context.getNamespaceURI("dc"), "http://purl.org/dc/elements/1.1/");
        assertEquals(context.getPrefix("http://purl.org/dc/elements/1.1/"), "dc");
        IRI dc = IRI.create("http://purl.org/dc/elements/1.1/creator");
        assertEquals(context.compact(dc), "dc:creator");
    }

}
