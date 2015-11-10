package org.xbib.tools.feed.elasticsearch.gnd;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.Test;
import org.xbib.elasticsearch.helper.client.Ingest;
import org.xbib.elasticsearch.helper.client.mock.MockTransportClient;
import org.xbib.iri.IRI;
import org.xbib.iri.namespace.IRINamespaceContext;
import org.xbib.rdf.content.RdfXContentFactory;
import org.xbib.rdf.content.RouteRdfXContentParams;
import org.xbib.rdf.io.turtle.TurtleContentParser;

import java.io.InputStream;

import static org.xbib.rdf.content.RdfXContentFactory.routeRdfXContentBuilder;

public class GNDTest {

    private final static Logger logger = LogManager.getLogger(GNDTest.class.getName());

    @Test
    public void testTurtleGND() throws Exception {

        IRINamespaceContext namespaceContext = IRINamespaceContext.newInstance();
        namespaceContext.addNamespace("dc", "http://purl.org/dc/elements/1.1/");
        namespaceContext.addNamespace("geo", "http://rdvocab.info/");
        namespaceContext.addNamespace("rda", "http://purl.org/dc/elements/1.1/");
        namespaceContext.addNamespace("foaf", "http://xmlns.com/foaf/0.1/");
        namespaceContext.addNamespace("sf", "http://www.opengis.net/ont/sf#");
        namespaceContext.addNamespace("isbd", "http://iflastandards.info/ns/isbd/elements/");
        namespaceContext.addNamespace("gndo", "http://d-nb.info/standards/elementset/gnd#");
        namespaceContext.addNamespace("dcterms", "http://purl.org/dc/terms/");
        namespaceContext.addNamespace("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
        namespaceContext.addNamespace("marcRole", "http://id.loc.gov/vocabulary/relators/");
        namespaceContext.addNamespace("lib", "http://purl.org/library/");
        namespaceContext.addNamespace("umbel", "http://umbel.org/umbel#");
        namespaceContext.addNamespace("bibo", "http://purl.org/ontology/bibo/");
        namespaceContext.addNamespace("owl", "http://www.w3.org/2002/07/owl#");
        namespaceContext.addNamespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        namespaceContext.addNamespace("skos", "http://www.w3.org/2004/02/skos/core#");

        Ingest ingest = new MockTransportClient();

        RouteRdfXContentParams params = new RouteRdfXContentParams(namespaceContext, "gnd", "gnd");
        params.setIdPredicate("gndo:gndIdentifier");
        params.setHandler((content, p) -> {
            int pos = p.getId().lastIndexOf('/');
            String docid = p.getId().substring(pos + 1);
            logger.info("{} {} {} content={}", p.getIndex(), p.getType(), docid, content);
            ingest.index(p.getIndex(), p.getType(),docid, content);

        });
        IRI base = IRI.builder().scheme("http").host("d-nb.info").path("/gnd/").build();
        InputStream in = getClass().getResourceAsStream("gnd.ttl");

        TurtleContentParser reader = new TurtleContentParser(in)
                .setBaseIRI(base)
                .context(namespaceContext);
        reader.setRdfContentBuilderProvider(() -> routeRdfXContentBuilder(params));
        reader.setRdfContentBuilderHandler(b -> {
            IRI iri = b.getSubject();
            String s = b.string();
        });
        reader.parse();
        in.close();
    }
}
