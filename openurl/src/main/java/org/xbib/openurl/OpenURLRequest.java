package org.xbib.openurl;

/**
 * The class contains information from the request
 * that has been transformed into OpenURL.
 */
public interface OpenURLRequest {

    /**
     * ContextObjects represent, in the form of OpenURL API
     * classes, the sequence of Services implied in a web service
     * request. For example, the Transport might generate a login
     * ContextObject followed by an edit ContextObject, if it sees
     * that the Requester hasn't logged in yet.
     *
     * @return an array of ContextObjects
     */
    ContextObject[] getContextObjects();
}
