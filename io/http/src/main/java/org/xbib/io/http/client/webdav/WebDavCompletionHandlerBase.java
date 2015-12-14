package org.xbib.io.http.client.webdav;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xbib.io.http.client.AsyncHandler;
import org.xbib.io.http.client.HttpResponseBodyPart;
import org.xbib.io.http.client.HttpResponseHeaders;
import org.xbib.io.http.client.HttpResponseStatus;
import org.xbib.io.http.client.netty.NettyResponse;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Simple {@link AsyncHandler} that add support for WebDav's response manipulation.
 *
 * @param <T> the result type
 */
public abstract class WebDavCompletionHandlerBase<T> implements AsyncHandler<T> {

    private final List<HttpResponseBodyPart> bodyParts = Collections.synchronizedList(new ArrayList<HttpResponseBodyPart>());
    private HttpResponseStatus status;
    private HttpResponseHeaders headers;

    /**
     * {@inheritDoc}
     */
    @Override
    public final State onBodyPartReceived(final HttpResponseBodyPart content) throws Exception {
        bodyParts.add(content);
        return State.CONTINUE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final State onStatusReceived(final HttpResponseStatus status) throws Exception {
        this.status = status;
        return State.CONTINUE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final State onHeadersReceived(final HttpResponseHeaders headers) throws Exception {
        this.headers = headers;
        return State.CONTINUE;
    }

    private Document readXMLResponse(InputStream stream) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Document document = null;
        try {
            document = factory.newDocumentBuilder().parse(stream);
            parse(document);
        } catch (SAXException | IOException | ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
        return document;
    }

    private void parse(Document document) {
        Element element = document.getDocumentElement();
        NodeList statusNode = element.getElementsByTagName("status");
        for (int i = 0; i < statusNode.getLength(); i++) {
            Node node = statusNode.item(i);

            String value = node.getFirstChild().getNodeValue();
            int statusCode = Integer.valueOf(value.substring(value.indexOf(" "), value.lastIndexOf(" ")).trim());
            String statusText = value.substring(value.lastIndexOf(" "));
            status = new HttpStatusWrapper(status, statusText, statusCode);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final T onCompleted() throws Exception {
        if (status != null) {
            Document document = null;
            if (status.getStatusCode() == 207) {
                document = readXMLResponse(new NettyResponse(status, headers, bodyParts).getResponseBodyAsStream());
            }
            // recompute response as readXMLResponse->parse might have updated it
            return onCompleted(new WebDavResponse(new NettyResponse(status, headers, bodyParts), document));
        } else {
            throw new IllegalStateException("Status is null");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onThrowable(Throwable t) {
    }

    /**
     * Invoked once the HTTP response has been fully read.
     *
     * @param response The {@link WebDavResponse}
     * @return Type of the value that will be returned by the associated {@link java.util.concurrent.Future}
     * @throws Exception if something wrong happens
     */
    public abstract T onCompleted(WebDavResponse response) throws Exception;

    private class HttpStatusWrapper extends HttpResponseStatus {

        private final HttpResponseStatus wrapped;

        private final String statusText;

        private final int statusCode;

        public HttpStatusWrapper(HttpResponseStatus wrapper, String statusText, int statusCode) {
            super(wrapper.getUri(), null);
            this.wrapped = wrapper;
            this.statusText = statusText;
            this.statusCode = statusCode;
        }

        @Override
        public int getStatusCode() {
            return (statusText == null ? wrapped.getStatusCode() : statusCode);
        }

        @Override
        public String getStatusText() {
            return (statusText == null ? wrapped.getStatusText() : statusText);
        }

        @Override
        public String getProtocolName() {
            return wrapped.getProtocolName();
        }

        @Override
        public int getProtocolMajorVersion() {
            return wrapped.getProtocolMajorVersion();
        }

        @Override
        public int getProtocolMinorVersion() {
            return wrapped.getProtocolMinorVersion();
        }

        @Override
        public String getProtocolText() {
            return wrapped.getStatusText();
        }

        @Override
        public SocketAddress getRemoteAddress() {
            return wrapped.getRemoteAddress();
        }

        @Override
        public SocketAddress getLocalAddress() {
            return wrapped.getLocalAddress();
        }
    }
}
