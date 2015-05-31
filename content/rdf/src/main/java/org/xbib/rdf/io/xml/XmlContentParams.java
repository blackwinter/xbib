package org.xbib.rdf.io.xml;

import org.xbib.iri.namespace.IRINamespaceContext;
import org.xbib.rdf.RdfContentParams;

public class XmlContentParams implements RdfContentParams {

    protected final static IRINamespaceContext defaultNamespaceContext = IRINamespaceContext.newInstance();

    private final IRINamespaceContext namespaceContext;

    public final static XmlContentParams DEFAULT_PARAMS = new XmlContentParams(defaultNamespaceContext);

    public XmlContentParams(IRINamespaceContext namespaceContext) {
        this.namespaceContext = namespaceContext;
    }

    public IRINamespaceContext getNamespaceContext() {
        return namespaceContext;
    }

}
