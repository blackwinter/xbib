package org.xbib.openurl;

import java.net.URI;

/**
 * Registered OpenURL Format URIs
 */
public interface Format {

    /**
     * Key/Encoded Value Format URI
     */
    URI FORMAT_KEV_URI = URI.create("info:ofi/fmt:kev");
    /**
     * Key/Encoded Value Format Matrix URI
     */
    URI FORMAT_KEV_MATRIX_URI = URI.create("info:ofi/fmt:kev:mtx");

    /**
     * Key/Encoded Value Format Matrix Constraint URI
     */
    URI FORMAT_KEV_MATRIX_CONSTRAINT_URI = URI.create("info:ofi/fmt:kev:mtx:ctx");

    /**
     * Key/Encoded Value Format Matrix Book URI
     */
    URI FORMAT_KEV_MATRIX_BOOK_URI = URI.create("info:ofi/fmt:kev:mtx:book");

    /**
     * Key/Encoded Value Format Matrix Journal URI
     */
    URI FORMAT_KEV_MATRIX_JOURNAL_URI = URI.create("info:ofi/fmt:kev:mtx:journal");

    /**
     * Key/Encoded Value Format Matrix Dissertation URI
     */
    URI FORMAT_KEV_MATRIX_DISSERTATION_URI = URI.create("info:ofi/fmt:kev:mtx:dissertation");

    /**
     * Key/Encoded Value Format Matrix Patent URI
     */
    URI FORMAT_KEV_MATRIX_PATENT_URI = URI.create("info:ofi/fmt:kev:mtx:patent");

    /**
     * XML Format URI
     */
    URI FORMAT_XML_URI = URI.create("info:ofi/fmt:xml:xsd");

    /**
     * XML Format Context URI
     */
    URI FORMAT_XML_CONTEXT_URI = URI.create("info:ofi/fmt:xml:xsd:ctx");

    /**
     * XML Format Book URI
     */
    URI FORMAT_XML_BOOK_URI = URI.create("info:ofi/fmt:xml:xsd:book");

    /**
     * XML Format Journal URI
     */
    URI FORMAT_XML_JOURNAL_URI = URI.create("info:ofi/fmt:xml:xsd:journal");

    /**
     * XML Format dissertation URI
     */
    URI FORMAT_XML_DISSERTATION_URI = URI.create("info:ofi/fmt:xml:xsd:dissertation");

    /**
     * XMl Formt patent URI
     */
    URI FORMAT_XML_PATENT_URI = URI.create("info:ofi/fmt:xml:xsd:patent");

}
