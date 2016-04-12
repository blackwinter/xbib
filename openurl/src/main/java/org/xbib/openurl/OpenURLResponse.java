package org.xbib.openurl;

import java.util.Map;

/**
 * Service classes are expected to return an instance of
 * this class.
 */
public class OpenURLResponse {

    private int status;
    private String redirectURL;
    private String contentType;
    private Map sessionMap;
    private Map headerMap;

    /**
     * Constructs a proxy for an HTTP response
     *
     * @param status
     */
    public OpenURLResponse(int status) {
        this(status, null, (Map) null);
    }

    /**
     * Constructs a proxy for an HTTP redirect response
     *
     * @param status
     * @param redirectURL
     */
    public OpenURLResponse(int status, String redirectURL) {
        this(status, redirectURL, (Map) null);
    }

    /**
     * @param status
     * @param redirectURL
     * @param sessionMap
     */
    public OpenURLResponse(int status, String redirectURL, Map sessionMap) {
        this.status = status;
        this.redirectURL = redirectURL;
        this.sessionMap = sessionMap;
    }

    /**
     * @param status
     * @param redirectURL
     * @param sessionMap
     * @param headerMap
     */
    public OpenURLResponse(int status, String contentType, String redirectURL, Map sessionMap,
                           Map headerMap) {
        this.status = status;
        this.contentType = contentType;
        this.redirectURL = redirectURL;
        this.sessionMap = sessionMap;
        this.headerMap = headerMap;
    }

    /**
     * Proxy for HttpServletResponse.setStatus()
     *
     * @return a HttpServletResponse server code
     */
    public int getStatus() {
        return status;
    }

    /**
     * Proxy for HttpServletResponse.sendRedirect()
     *
     * @return the target URL
     */
    public String getRedirectURL() {
        return redirectURL;
    }

    /**
     * Proxy for HttpServletResponse.setContentType()
     *
     * @return a String specifying the MIME type of the content
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Get session map
     *
     * @return the session map
     */
    public Map getSessionMap() {
        return sessionMap;
    }

    /**
     * Get header map
     *
     * @return the header map
     */
    public Map getHeaderMap() {
        return headerMap;
    }
}