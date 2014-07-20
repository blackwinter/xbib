package org.asynchttpclient;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the asynchronous HTTP response callback for an {@link org.asynchttpclient.AsyncCompletionHandler}
 */
public interface Response {
    /**
     * Returns the status code for the request.
     * 
     * @return The status code
     */
    int getStatusCode();

    /**
     * Returns the status text for the request.
     * 
     * @return The status text
     */
    String getStatusText();

    /**
     * Return the entire response body as a byte[].
     * 
     * @return the entire response body as a byte[].
     * @throws java.io.IOException
     */
    byte[] getResponseBodyAsBytes() throws IOException;

    /**
     * Return the entire response body as a ByteBuffer.
     * 
     * @return the entire response body as a ByteBuffer.
     * @throws java.io.IOException
     */
    ByteBuffer getResponseBodyAsByteBuffer() throws IOException;

    /**
     * Returns an input stream for the response body. Note that you should not try to get this more than once, and that you should not close the stream.
     * 
     * @return The input stream
     * @throws java.io.IOException
     */
    InputStream getResponseBodyAsStream() throws IOException;

    /**
     * Returns the first maxLength bytes of the response body as a string. Note that this does not check whether the content type is actually a textual one, but it will use the
     * charset if present in the content type header.
     * 
     * @param maxLength
     *            The maximum number of bytes to read
     * @param charset
     *            the charset to use when decoding the stream
     * @return The response body
     * @throws java.io.IOException
     */
    String getResponseBodyExcerpt(int maxLength, String charset) throws IOException;

    /**
     * Return the entire response body as a String.
     * 
     * @param charset
     *            the charset to use when decoding the stream
     * @return the entire response body as a String.
     * @throws java.io.IOException
     */
    String getResponseBody(String charset) throws IOException;

    /**
     * Returns the first maxLength bytes of the response body as a string. Note that this does not check whether the content type is actually a textual one, but it will use the
     * charset if present in the content type header.
     * 
     * @param maxLength
     *            The maximum number of bytes to read
     * @return The response body
     * @throws java.io.IOException
     */
    String getResponseBodyExcerpt(int maxLength) throws IOException;

    /**
     * Return the entire response body as a String.
     * 
     * @return the entire response body as a String.
     * @throws java.io.IOException
     */
    String getResponseBody() throws IOException;

    /**
     * Return the request {@link java.net.URI}. Note that if the request got redirected, the value of the {@link java.net.URI} will be the last valid redirect url.
     * 
     * @return the request {@link java.net.URI}.
     * @throws java.net.MalformedURLException
     */
    URI getUri() throws MalformedURLException;

    /**
     * Return the content-type header value.
     * 
     * @return the content-type header value.
     */
    String getContentType();

    /**
     * Return the response header
     * 
     * @return the response header
     */
    String getHeader(String name);

    /**
     * Return a {@link java.util.List} of the response header value.
     * 
     * @return the response header
     */
    List<String> getHeaders(String name);

    FluentCaseInsensitiveStringsMap getHeaders();

    /**
     * Return true if the response redirects to another object.
     * 
     * @return True if the response redirects to another object.
     */
    boolean isRedirected();

    /**
     * Subclasses SHOULD implement toString() in a way that identifies the request for logging.
     * 
     * @return The textual representation
     */
    String toString();

    /**
     * Return the list of {@link org.asynchttpclient.Cookie}.
     */
    List<Cookie> getCookies();

    /**
     * Return true if the response's status has been computed by an {@link org.asynchttpclient.AsyncHandler}
     * 
     * @return true if the response's status has been computed by an {@link org.asynchttpclient.AsyncHandler}
     */
    boolean hasResponseStatus();

    /**
     * Return true if the response's headers has been computed by an {@link org.asynchttpclient.AsyncHandler} It will return false if the either
     * {@link org.asynchttpclient.AsyncHandler#onStatusReceived(org.asynchttpclient.HttpResponseStatus)} or {@link org.asynchttpclient.AsyncHandler#onHeadersReceived(org.asynchttpclient.HttpResponseHeaders)} returned {@link org.asynchttpclient.AsyncHandler.STATE#ABORT}
     * 
     * @return true if the response's headers has been computed by an {@link org.asynchttpclient.AsyncHandler}
     */
    boolean hasResponseHeaders();

    /**
     * Return true if the response's body has been computed by an {@link org.asynchttpclient.AsyncHandler}. It will return false if the either {@link org.asynchttpclient.AsyncHandler#onStatusReceived(org.asynchttpclient.HttpResponseStatus)}
     * or {@link org.asynchttpclient.AsyncHandler#onHeadersReceived(org.asynchttpclient.HttpResponseHeaders)} returned {@link org.asynchttpclient.AsyncHandler.STATE#ABORT}
     * 
     * @return true if the response's body has been computed by an {@link org.asynchttpclient.AsyncHandler}
     */
    boolean hasResponseBody();

    public static class ResponseBuilder {
        private final List<HttpResponseBodyPart> bodyParts = new ArrayList<HttpResponseBodyPart>();
        private HttpResponseStatus status;
        private HttpResponseHeaders headers;

        public ResponseBuilder accumulate(HttpResponseStatus status) {
            this.status = status;
            return this;
        }

        public ResponseBuilder accumulate(HttpResponseHeaders headers) {
            this.headers = headers;
            return this;
        }

        public ResponseBuilder accumulate(HttpResponseBodyPart bodyPart) {
            bodyParts.add(bodyPart);
            return this;
        }

        /**
         * Build a {@link org.asynchttpclient.Response} instance
       * 
         * @return a {@link org.asynchttpclient.Response} instance
         */
        public Response build() {
            return status == null ? null : status.prepareResponse(headers, bodyParts);
        }

        /**
         * Reset the internal state of this builder.
         */
        public void reset() {
            bodyParts.clear();
            status = null;
            headers = null;
        }
    }

}