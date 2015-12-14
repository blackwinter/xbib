package org.xbib.io.http.client.webdav;

import io.netty.handler.codec.http.HttpHeaders;
import org.w3c.dom.Document;
import org.xbib.io.http.client.Response;
import org.xbib.io.http.client.cookie.Cookie;
import org.xbib.io.http.client.uri.Uri;

import java.io.InputStream;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.List;

/**
 * Customized {@link Response} which add support for getting the response's body as an XML document (@link
 * WebDavResponse#getBodyAsXML}
 */
public class WebDavResponse implements Response {

    private final Response response;
    private final Document document;

    public WebDavResponse(Response response, Document document) {
        this.response = response;
        this.document = document;
    }

    public int getStatusCode() {
        return response.getStatusCode();
    }

    public String getStatusText() {
        return response.getStatusText();
    }

    @Override
    public byte[] getResponseBodyAsBytes() {
        return response.getResponseBodyAsBytes();
    }

    public ByteBuffer getResponseBodyAsByteBuffer() {
        return response.getResponseBodyAsByteBuffer();
    }

    public InputStream getResponseBodyAsStream() {
        return response.getResponseBodyAsStream();
    }

    public String getResponseBody() {
        return response.getResponseBody();
    }

    public String getResponseBody(Charset charset) {
        return response.getResponseBody(charset);
    }

    public Uri getUri() {
        return response.getUri();
    }

    public String getContentType() {
        return response.getContentType();
    }

    public String getHeader(String name) {
        return response.getHeader(name);
    }

    public List<String> getHeaders(String name) {
        return response.getHeaders(name);
    }

    public HttpHeaders getHeaders() {
        return response.getHeaders();
    }

    public boolean isRedirected() {
        return response.isRedirected();
    }

    public List<Cookie> getCookies() {
        return response.getCookies();
    }

    public boolean hasResponseStatus() {
        return response.hasResponseStatus();
    }

    public boolean hasResponseHeaders() {
        return response.hasResponseHeaders();
    }

    public boolean hasResponseBody() {
        return response.hasResponseBody();
    }

    public SocketAddress getRemoteAddress() {
        return response.getRemoteAddress();
    }

    public SocketAddress getLocalAddress() {
        return response.getLocalAddress();
    }

    public Document getBodyAsXML() {
        return document;
    }
}
