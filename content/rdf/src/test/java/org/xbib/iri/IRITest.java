package org.xbib.iri;

import org.junit.Assert;
import org.junit.Test;

public class IRITest extends Assert {

    @Test
    public void testJsonLd() {
        IRI iri = IRI.create("@context");
        assertEquals(null, iri.getScheme());
        assertEquals("@context",iri.getSchemeSpecificPart());
    }

    @Test(expected = org.xbib.iri.IRISyntaxException.class)
    public void testIllegalBlankNodeIRI() {
        IRI iri = IRI.create("_:a1");
        assertEquals("_", iri.getScheme());
        assertEquals("a1",iri.getSchemeSpecificPart());
    }

    @Test
    public void testRoutingByIRI() {
        IRI iri = IRI.create("http://index?type#id");
        assertEquals("http", iri.getScheme());
        assertEquals("index", iri.getHost());
        assertEquals("type", iri.getQuery());
        assertEquals("id", iri.getFragment());
    }

    @Test
    public void testCuri() {
        IRI curi = IRI.builder().curie("dc:creator").build();
        assertEquals("dc", curi.getScheme());
        assertEquals("creator", curi.getPath());
        curi = IRI.builder().curie("creator").build();
        assertNull(curi.getScheme());
        assertEquals("creator", curi.getPath());
    }

    @Test
    public void testSchemeSpecificPart() {
        IRI curi = IRI.builder().curie("dc:creator").build();
        assertEquals("dc", curi.getScheme());
        assertEquals("creator", curi.getSchemeSpecificPart());
        assertEquals("dc:creator", curi.toString());
        curi = IRI.builder().curie("creator").build();
        assertNull(curi.getScheme());
        assertEquals("creator", curi.getSchemeSpecificPart());
        assertEquals("creator", curi.toString());
    }

    @Test
    public void testIdentity() {
        String s = "urn:a";
        IRI i1 = IRI.builder().curie(s).build();
        IRI i2 = IRI.create(s);
        assertEquals(i1, i2);
    }
}
