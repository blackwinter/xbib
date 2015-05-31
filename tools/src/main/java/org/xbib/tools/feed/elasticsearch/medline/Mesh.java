package org.xbib.tools.feed.elasticsearch.medline;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.io.InputService;
import org.xbib.iri.IRI;
import org.xbib.iri.namespace.IRINamespaceContext;
import org.xbib.pipeline.Pipeline;
import org.xbib.pipeline.PipelineProvider;
import org.xbib.rdf.RdfContentBuilder;
import org.xbib.rdf.RdfContentParams;
import org.xbib.rdf.Resource;
import org.xbib.rdf.content.RdfXContentParams;
import org.xbib.rdf.content.RouteRdfXContentParams;
import org.xbib.rdf.io.xml.XmlContentParser;
import org.xbib.rdf.io.xml.XmlHandler;
import org.xbib.rdf.memory.MemoryResource;
import org.xbib.tools.Feeder;
import org.xbib.xml.InvalidXmlCharacterFilterReader;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import static org.xbib.rdf.content.RdfXContentFactory.routeRdfXContentBuilder;

public final class Mesh extends Feeder {

    private final static Logger logger = LogManager.getLogger(Mesh.class);

    @Override
    public String getName() {
        return "mesh-xml-elasticsearch";
    }

    @Override
    protected PipelineProvider<Pipeline> pipelineProvider() {
        return Mesh::new;
    }

    @Override
    public void process(URI uri) throws Exception {
        RdfContentParams params = new RdfXContentParams();
        InputStream in = InputService.getInputStream(uri);
        Handler handler = new Handler(params).setDefaultNamespace("", "http://xbib.org/ns/mesh/");
        new XmlContentParser(new InvalidXmlCharacterFilterReader(in, "UTF-8")).setNamespaces(false).setHandler(handler).parse();

        in.close();
    }

    private class Handler extends DefaultHandler implements XmlHandler {

        private final RdfContentParams params;

        private final StringBuilder content = new StringBuilder();

        private final Stack<QName> parents = new Stack<QName>();

        private Resource resource;

        private String defaultPrefix;

        private String defaultNamespace;

        private String descriptor;

        private List<String> variants = new LinkedList<>();

        public Handler(RdfContentParams params) {
            this.params = params;
            this.resource = new MemoryResource();
        }

        public RdfContentParams getParams() {
            return params;
        }

        public Resource getResource() {
            return resource;
        }

        @Override
        public XmlHandler setNamespaceContext(IRINamespaceContext namespaceContext) {
            return this;
        }

        @Override
        public IRINamespaceContext getNamespaceContext() {
            return params.getNamespaceContext();
        }

        public Handler setDefaultNamespace(String prefix, String namespaceURI) {
            this.defaultPrefix = prefix;
            this.defaultNamespace = namespaceURI;
            params.getNamespaceContext().addNamespace(prefix, namespaceURI);
            return this;
        }

        @Override
        public XmlHandler setBuilder(RdfContentBuilder builder) {
            // ignore
            return this;
        }

        @Override
        public void startDocument() throws SAXException {
            try {
                openResource();
            } catch (IOException e) {
                throw new SAXException(e);
            }
            parents.push(new QName("_"));
        }

        @Override
        public void endDocument() throws SAXException {
            try {
                closeResource();
            } catch (IOException e) {
                throw new SAXException(e);
            }
        }

        @Override
        public void startElement(String nsURI, String localname, String qname, Attributes atts) throws SAXException {
            try {
                QName name = makeQName(nsURI, localname, qname);
                boolean delimiter = isResourceDelimiter(name);
                if (delimiter) {
                    closeResource();
                    openResource();
                }
                if (skip(name)) {
                    return;
                }
                parents.push(name);
            } catch (Exception e) {
                throw new SAXException(e);
            }
        }

        @Override
        public void endElement(String nsURI, String localname, String qname) throws SAXException {
            QName name = makeQName(nsURI, localname, qname);
            if (skip(name)) {
                content.setLength(0);
                return;
            }
            parents.pop();
            if ("DescriptorUI".equals(name.getLocalPart())) {
                resource.id(IRI.create(content()));
            }
            if (!isResourceDelimiter(name) && !parents.isEmpty()) {
                if ("String".equals(name.getLocalPart()) && "DescriptorName".equals(parents.peek().getLocalPart())) {
                    String content = content();
                    int pos = content.indexOf('[');
                    if (pos > 0) {
                        descriptor = content.substring(pos + 1, content.length() - 1);// without [ and ]
                        variants.add(content.substring(0, pos)); // german form
                    }
                }
            }
            content.setLength(0);
        }

        @Override
        public void characters(char[] chars, int start, int length) throws SAXException {
            content.append(new String(chars, start, length));
        }

        @Override
        public void startPrefixMapping(String prefix, String uri) throws SAXException {
        }

        @Override
        public void endPrefixMapping(String prefix) throws SAXException {
        }

        protected String makePrefix(String name) {
            return name.replaceAll("[^a-zA-Z]+", "");
        }

        protected QName makeQName(String nsURI, String localname, String qname) {
            String prefix = params.getNamespaceContext().getPrefix(nsURI);
            return new QName(!isEmpty(nsURI) ? nsURI : defaultNamespace,
                    !isEmpty(localname) ? localname : qname,
                    !isEmpty(prefix) ? prefix : defaultPrefix);
        }

        public String content() {
            String s = content.toString().trim();
            return s.length() > 0 ? s : null;
        }

        protected void openResource() throws IOException {
            resource = new MemoryResource();
        }

        protected void closeResource() throws IOException {
            if (descriptor != null) {
                resource.add("descriptor", descriptor);
                variants.forEach(s -> resource.add("variant", s));
                descriptor = null;
                variants.clear();
            }
            boolean empty = resource.isEmpty();
            if (empty) {
                logger.warn("resource is empty");
                return;
            }
            RouteRdfXContentParams params = new RouteRdfXContentParams(getNamespaceContext(),
                    settings.get("index", "mesh"),
                    settings.get("type", "mesh"));
            params.setHandler((content, p) -> ingest.index(p.getIndex(), p.getType(), resource.id().toString(), content));
            RdfContentBuilder builder = routeRdfXContentBuilder(params);
            builder.receive(resource);
            if (settings.getAsBoolean("mock", false)) {
                logger.info("{} {}", resource.id(), params.getGenerator().get());
            }
        }

        private boolean isEmpty(String s) {
            return s == null || s.length() == 0;
        }

        public boolean isResourceDelimiter(QName name) {
            return "DescriptorRecord".equals(name.getLocalPart());
        }

        public boolean skip(QName name) {
            boolean isAttr = name.getLocalPart().startsWith("@");
            return "DescriptorRecordSet".equals(name.getLocalPart())
                    || "DescriptorRecord".equals(name.getLocalPart())
                    || isAttr;
        }
    }
}
